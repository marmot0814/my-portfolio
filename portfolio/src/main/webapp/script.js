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
  getUser();
  getComments();
}

function getComments() {
  fetch('/comment').then(response => response.json()).then((comments) => {
    const commentsContainer = document.getElementById('comments-container');
    comments.forEach((comment) => {
      commentsContainer.appendChild(createCommentElement(comment));
    });
  });
}

function createCommentElement(comment) {
  const liElement = document.createElement('li');
  liElement.innerText = comment.username + '[' + comment.timestamp + '] ' + ':' + comment.content;
  return liElement;
}

function getUser() {
  fetch('/user').then(response => response.json()).then((userStatus) => {

    const user_name = document.getElementById('user-name');
    const login_logout = document.getElementById('login-logout');

    user_name.innerText = "Hello " + userStatus.username + "!";
    login_logout.href = userStatus.url;
    login_logout.innerText = userStatus.loginLogoutAction;

  });
}