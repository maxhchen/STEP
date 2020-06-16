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
import java.util.*;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<TimeRange> results = new ArrayList<>();

    List<Event> allEvents = new ArrayList(events);
    Collection<String> allAttendees = request.getAttendees();
    int numAttendees = allAttendees.size();
    long meetingTime = request.getDuration();

    // Base Case: No required attendees.
    if (numAttendees == 0) {
        results = Arrays.asList(TimeRange.WHOLE_DAY);
        return results;
    // Base Case: Invalid meeting time.
    } else if (meetingTime == 0 || meetingTime > TimeRange.WHOLE_DAY.duration()) {
        results = Arrays.asList();
        return results;
    }

    // Remove events if the people attending them aren't actually looking
    // to book a room.
    Iterator<Event> iter = allEvents.iterator();

    while (iter.hasNext()) {
        Event e = iter.next();
        if (eventAttendeeNotMeeting(e, allAttendees)) {
            iter.remove();
        }
    }

    // Sort Events by their start time.
    Comparator<Event> EventStartComparator
        = Comparator.comparing(Event::getWhen, (e1, e2) -> {
            return Long.compare(e1.start(), e2.start());
        });

    Collections.sort(allEvents, EventStartComparator);
    
    // Track latest end time of any event.
    // Since eventTimes was sorted by start, the event with the latest end time
    // might come before the last event in the List (e.g. if we had [5, 20]
    // followed by [10, 15], we want to use [20, END_OF_DAY] for the final gap,
    // not [15, END_OF_DAY]).
    int endOfLatestEvent = 0;
    TimeRange gap;

    // Iteratively construct gaps between busy times.
    for (int i = 0; i < allEvents.size(); i++) {        
        // First gap before the first time anyone is busy.
        if (i == 0) {
            gap = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, allEvents.get(i).getWhen().start(), false);
        // gaps in the middle.
        } else {
            gap = TimeRange.fromStartEnd(allEvents.get(i-1).getWhen().end(), allEvents.get(i).getWhen().start(), false);
        }

        // Validate gap.
        if (isValidGap(gap, meetingTime)) {
            results.add(gap);
        }

        // Update latest end time of any event.
        if (i != allEvents.size()) {
            endOfLatestEvent = endOfLatestEvent > allEvents.get(i).getWhen().end() ? endOfLatestEvent : allEvents.get(i).getWhen().end();
        }
    }
    
    // Last gap after the last person is busy.
    gap = TimeRange.fromStartEnd(endOfLatestEvent, TimeRange.END_OF_DAY, true);
    if (isValidGap(gap, meetingTime)) {
        results.add(gap);
    }

    return results;

  }

  // Return `true` if a gap is "well-constructed" / valid:
  // 1) Start comes before end
  // 2) gap is big enough to have meeting
  private boolean isValidGap(TimeRange gap, long meetingTime) {
    return gap.start() < gap.end()
        && gap.duration() >= meetingTime;
    }

  // Return `true` if person attending the event isn't one of the people looking
  // to book a meeting i.e. gaps can be scheduled during the event.
  private boolean eventAttendeeNotMeeting(Event e, Collection<String> allAttendees) {
    Set<String> attendees = e.getAttendees();
    for (String s : attendees) {
        if (! allAttendees.contains(s)) {
            return true;
        }
    }
    return false;
  }
}
