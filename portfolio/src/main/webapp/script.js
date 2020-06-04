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

// Fetch text from /data URL.
function fetchMessage() {
  fetch("/data").then(response => response.text()).then(quote => {
    document.getElementById('fetch-container').innerText = quote;
  });
}

// Gets the list of all existing comments and displays them.
function loadComments() {
  fetch('/data').then(response => response.json()).then(allComments => {
    const commentContainer = document.getElementById('comment-container');
    allComments.forEach(commentText => {
      const commentItem = createCommentItem(commentText.text);
      commentContainer.appendChild(commentItem);
    });
  });
}

// Helper function to instantiate each comment.
function createCommentItem(text) {
  const commentItem = document.createElement('div');
  commentItem.innerText = text;
  commentItem.className = "comment-item";
  return commentItem;
}
