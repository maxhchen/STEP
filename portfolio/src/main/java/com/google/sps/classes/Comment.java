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

package com.google.sps.classes;

/*Private class used to represent comments as objects in Datastore. */
public class Comment {
    /*
    long id:            unique id for the Comment in Datastore
    String text:        text in Comment
    String timestamp:   timestamp for when the Comment was sent (Month Date, Year H:MM:SS AM/PM)
    */
    private long id;
    private String text;
    private String timestamp;
    private String email;

    public Comment(long id, String text, String timestamp, String email) {
        this.id = id;
        this.text = text;
        this.timestamp = timestamp;
        this.email = email;
    }

    /* Getter method for text. */
    public String getText() {
        return this.text;
    }
    /* Getter method for timestamp. */
    public String getTimestamp() {
        return this.timestamp;
    }
    /* Getter method for email. */
    public String getEmail() {
        return this.email;
    }
}
