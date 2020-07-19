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

function getRandomQuote() {
  fetch('/random-quote').then(response => response.text()).then((quote) => {
    document.getElementById('quote-container').innerText = quote;
  });
}

function initial() {
  getComments();
  getUser();
}

function getComments() {
  fetch('/comment').then(response => response.json()).then((comments) => {
    const commentsContainer = document.getElementById('comments-container');
    const title = document.createElement('h3');
    title.innerText = comments.length + " Comment" + (comments.length > 1 ? "s" : "");
    commentsContainer.appendChild(title);
    comments.forEach((comment) => {
      commentsContainer.appendChild(createCommentElement(comment));
    });
  });
}

function createCommentElement(comment) {
  const aElement = document.createElement('a');
  aElement.className = "list-group-item";
  aElement.href = "#";

  const timeElement = document.createElement('span');
  timeElement.className = "label label-default";
  timeElement.innerText = comment.timestamp;

  const usernameElement = document.createElement('h4');
  usernameElement.className = "list-group-item-heading";
  usernameElement.innerText = comment.username;

  const contentElement = document.createElement('p');
  contentElement.className = "list-group-item-text";
  contentElement.innerText = comment.content;

  aElement.appendChild(usernameElement);
  aElement.appendChild(timeElement);
  aElement.appendChild(contentElement);

  return aElement;
}

function getUser() {
  fetch('/user').then(response => response.json()).then((userStatus) => {
    const user_name = document.getElementById('user-name');
    const login_logout = document.getElementById('login-logout');

    user_name.innerText = userStatus.username;
    login_logout.href = userStatus.url;
    login_logout.innerText = userStatus.loginLogoutAction;

    if (userStatus.loginLogoutAction == "Logout") {
      const commentInputField = document.getElementById('comment-input-field');
      commentInputField.hidden = false;
    }
    if (userStatus.loginLogoutAction == "Login") {
      const commentInputField = document.getElementById('login-alert');
      commentInputField.hidden = false;
    }
  });
}