// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    
    // Collect target attendees into a set in order to fast the query instruction.
    Set<String> attendees = new HashSet<String>(request.getAttendees());

    /*  Create a sorted map. It contains time range information.
     *  If we have three time range. We will get a time slot map like below.
     *  For each timerange, we make the time slot map +1 at the start time, and make the time slot map -1 at the end time.
     *
     *             begin time                                            end time
     *                  |                                                   |
     *  TimeRange A     |      |--------------|                             |
     *  TimeSlot for A  |     +1             -1                             |
     *  TimeRange B     |                          |----------------|       |
     *  TimeSlot for B  |                         +1               -1       |
     *  TimeRange C     |           |---------|                             |
     *  TimeSlot for C  |          +1        -1                             |
     *
     *  Total TimeSlot  0     +1   +1        -2   +1               -1       0
     *
     *  Therefore, we can iterate from left the right and sum it to get the number of segment covered now.
     *
     *             begin time                                            end time
     *  Iterate result  0      1    2         0    1                0       0
     *  # of segments   |--0---|-1--|----2----|-0--|-------1--------|---0---|
     *
     *  And we choose the time ranges that no segment covers on them.
     */
    Map<Integer, Integer> timeSlot = new TreeMap<Integer, Integer>();

    for (Event event : events) {

      // Check if there are some target people involved in the current event.
      boolean hasRelatedPerson = false;
      for (String attendee : event.getAttendees())
        if (attendees.contains(attendee))
          hasRelatedPerson = true;
      
      // If no one related to this event, ignore it.
      if (!hasRelatedPerson)
        continue;

      TimeRange when = event.getWhen();

      // Add 1 at the start time of the event in the time slot map.
      if (timeSlot.get(when.start()) == null) 
        timeSlot.put(when.start(), 0);
      timeSlot.put(when.start(), timeSlot.get(when.start()) + 1);

      // Minus 1 at the end time of the event in the time slot map.
      if (timeSlot.get(when.end()) == null)
        timeSlot.put(when.end(), 0);
      timeSlot.put(when.end(), timeSlot.get(when.end()) - 1);
    }

    ArrayList<TimeRange> availableTimeRanges = new ArrayList<>();

    // Maintain sum to get the # of segments covered now.
    Integer sum = 0;
    
    // Maintain prev to get the start point of an empty time range.
    Integer prev = 0;

    for(Map.Entry<Integer,Integer> entry : timeSlot.entrySet()) {
      Integer key = entry.getKey();
      Integer value = entry.getValue();

      if (sum > 0 && sum + value == 0) {
        /*  start a new empty time range
         *  TimeRange A               ------|
         *  Time Slot                      -1
         *  # of segment covered     ----1--|---0------
         *                                  ^
         *                           current position
         */
        prev = key;
      } else if (sum == 0 && sum + value > 0) {
        /* end a new empty time range
         * TimeRange A                      |-------
         * Time Slot                       +1
         * # of segment covered      ---0---|---1--------
         *                                  ^
         *                           current position
         */
        Integer duration = key - prev;

        // Check the duration is long enough or not.
        if (duration >= request.getDuration())
          availableTimeRanges.add(TimeRange.fromStartDuration(prev, duration));
      }

      // sum the current Time Slot value to maintain the current # of segments covered now.
      sum += value;
    }

    /*  Tail case should be considered.
     *                                          end time
     *  TimeRange A            ---|                |
     *  Time Slot                -1                0
     *  # of segment covered  --1-|------0---------|
     *                            ^
     *                     current position
     */
    Integer duration = TimeRange.WHOLE_DAY.end() - prev;
    if (duration >= request.getDuration())
      availableTimeRanges.add(TimeRange.fromStartDuration(prev, duration));
      
    return availableTimeRanges;
  }
}
