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

var used_greetings = [];
var greetings =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!', 'Hallo Welt!', 'こんにちは世界', '안녕하세요 세계!', 'Привет мир!'];

function addRandomGreeting() {
  // Pick a random greeting.
  let greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add selected greeting to list of used greetings.
  used_greetings.push(greeting);

  // Add it to the page.
  let greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;

  // Prevent repeats before all messages are seen once.
  greetings.splice(greetings.indexOf(greeting), 1);
  if (greetings.length == 0) {
      greetings = used_greetings;
      used_greetings = [];
  }
}
