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
    
    Set<String> attendees = new HashSet<String>(request.getAttendees());

    Map<Integer, Integer> timeSlot = new TreeMap<Integer, Integer>();

    for (Event event : events) {
      TimeRange when = event.getWhen();

      boolean hasRelatedPerson = false;
      for (String attendee : event.getAttendees())
        if (attendees.contains(attendee))
          hasRelatedPerson = true;
      
      if (!hasRelatedPerson)
        continue;

      if (timeSlot.get(when.start()) == null) 
        timeSlot.put(when.start(), 0);
      timeSlot.put(when.start(), timeSlot.get(when.start()) + 1);

      if (timeSlot.get(when.end()) == null)
        timeSlot.put(when.end(), 0);
      timeSlot.put(when.end(), timeSlot.get(when.end()) - 1);
    }

    ArrayList<TimeRange> availableTimeRanges = new ArrayList<>();
    Integer sum = 0, prev = 0;

    for(Map.Entry<Integer,Integer> entry : timeSlot.entrySet()) {
      Integer key = entry.getKey();
      Integer value = entry.getValue();

      if (sum > 0 && sum + value == 0) {
        // start a new empty time range
        prev = key;
      } else if (sum == 0 && sum + value > 0) {
        // end a new empty time range
        Integer duration = key - prev;
        if (duration >= request.getDuration())
          availableTimeRanges.add(TimeRange.fromStartDuration(prev, duration));
      }
      sum += value;
    }

    // tail
    Integer duration = TimeRange.WHOLE_DAY.end() - prev;
    if (duration >= request.getDuration())
      availableTimeRanges.add(TimeRange.fromStartDuration(prev, duration));
      
    return availableTimeRanges;
  }
}
