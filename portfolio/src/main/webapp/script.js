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

/**
 * Adds a random greeting to the page.
 */

var all_greetings =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!', 'Hallo Welt!', 'こんにちは世界', '안녕하세요 세계!', 'Привет мир!'];
var active_greetings = all_greetings.slice();

function addRandomGreeting() {
  // Pick a random greeting.
  const greeting = active_greetings[Math.floor(Math.random() * active_greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;

  // Prevent repeats before all messages are seen once.
  active_greetings.splice(active_greetings.indexOf(greeting), 1);
  if (active_greetings.length == 0) {
      active_greetings = all_greetings.slice();
  }
}

// Parses URL to determine number of comments to display.
function loadCommentsOnStart() {
  let commentLimit = (new URL(document.location)).searchParams.get('commentLimit');
  loadComments(commentLimit);
}

// Load `commentLimit` comments.
function loadComments(commentLimit) {
    const URL = '/data?commentLimit=' + commentLimit;
    fetch(URL).then(response => response.json()).then(allComments => {
    const commentContainer = document.getElementById('comment-container');

    // Prevent visual "glitch" when comments show up twice.
    commentContainer.innerHTML = "";

    allComments.forEach(commentText => {
      const commentItem = createCommentItem(commentText.text, commentText.timestamp, commentText.email);
      commentContainer.appendChild(commentItem);
    });
  });
}

// Helper function to instantiate each comment.
function createCommentItem(text, timestamp, email) {
  const commentItem = document.createElement('div');
  commentItem.innerText = "\"" + text + "\" sent on " + timestamp + " by " + email;
  commentItem.className = "comment-item";
  return commentItem;
}

// Get login status of user and either display hidden elements or display login form.
function fetchLoginStatus() {
    fetch("/auth").then(response => response.json()).then(status => {

        if (status[0] == "true") {
            const hiddenElements = document.getElementsByClassName('hidden');
            Array.from(hiddenElements).forEach(item => {
                item.style.display = "block";
            });
            const logoutLink = document.getElementById('status-link');
            const logoutButton = document.getElementById('status-button');
            logoutLink.href = status[1];
            logoutButton.innerText = "Logout here";

        } else {
            const loginLink = document.getElementById('status-link');
            const loginButton = document.getElementById('status-button');
            loginLink.href = status[1];
            loginButton.innerText = "Login here";
        }
    });
}

// Wrapper function to run multiple functions on page load.
function onLoad() {
    loadCommentsOnStart();
    fetchLoginStatus();
}