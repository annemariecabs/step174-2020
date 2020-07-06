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

/** Creates an <li> element containing text. */
function createCommentList(inputText) {
  const listElement = document.createElement('li');
  listElement.innerText = inputText;
  return listElement;
}

/**
 * Fetches comments for display.
 */
function getCommentData() {
  fetch('/data')                          // sends a request to /data
      .then(response => response.json())  // parses the response as JSON
      .then((myComments) => {  // now we can reference the fields in myComments!
        const commentsElement = document.getElementById('quote-container');
        commentsElement.innerHTML = '';
        for (var increment = 0; increment < myComments.length; increment += 1) {
          commentsElement.appendChild(createCommentList(myComments[increment]));
        }
      });
}
