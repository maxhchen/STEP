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
  /** Finds and returns a collection of non-overlapping time intervals that are
  * each at least as long as needed for the required attendees to have a meeting
  * of the desired length. An empty list is returned if no such times exist.
  *
  * @param events    - Collection of events signifying when an attendee cannot meet
  * @param request   - Request specifying the meeting's attendees and duration
  *                                 - Contains both required and optional attendees
  *
  * @return          - Collection of non-overlapping time intervals
  */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<Event> allEvents                   = new ArrayList(events);
    Collection<String> allAttendees         = request.getAttendees();
    Collection<String> allOptionalAttendees = request.getOptionalAttendees();
    int numAttendees                        = allAttendees.size();
    int numOptionalAttendees                = allOptionalAttendees.size();
    long meetingTime                        = request.getDuration();

    // Base Case: No attendees.
    if (numAttendees == 0 && numOptionalAttendees == 0) {
        return Arrays.asList(TimeRange.WHOLE_DAY);
    // Base Case: Invalid meeting time.
    } else if (meetingTime == 0 || meetingTime > TimeRange.WHOLE_DAY.duration()) {
        return Arrays.asList();

    }
    // Remove events if no attendees are actually going to them.
    allEvents = removePeopleNotAttending(allEvents, allAttendees, allOptionalAttendees);

    // Create list of events that doesn't consider optional attendees.
    List<Event> eventsWithoutOptionalAttendees = getEventsWithoutOptionalAttendees(allEvents, allOptionalAttendees);

    // Custom comparator to sort events by start time.
    Comparator<Event> EventStartTimeComparator
        = Comparator.comparing(Event::getWhen, (e1, e2) -> {
            return Long.compare(e1.start(), e2.start());
        });

    Collections.sort(allEvents, EventStartTimeComparator);
    Collections.sort(eventsWithoutOptionalAttendees, EventStartTimeComparator);

    // Find valid gaps if we considered everyone.
    Collection<TimeRange> gapsConsideringEveryone = findGaps(allEvents, meetingTime, numAttendees + numOptionalAttendees);

    if (gapsConsideringEveryone.size() == 0) {
        // If it's impossible to find any gaps when considering everyone,
        // find valid gaps without considering optional attendees and return that instead.
        Collection<TimeRange> gapsWithoutOptional = findGaps(eventsWithoutOptionalAttendees, meetingTime, numAttendees);
        return gapsWithoutOptional;
    } else {
        // If there is at least one valid gap that considers everyone, that is better than
        // having more valid gaps that exclude optional attendees, so we default to returning gaps
        // that consider everyone.
        return gapsConsideringEveryone;
    }
  }

  /** Discard an event if none of the attendees who want to book a meeting are going to it. */
  private List<Event> removePeopleNotAttending(List<Event> allEvents, Collection<String> allAttendees, Collection<String> allOptionalAttendees) {
    Iterator<Event> allEventsIter = allEvents.iterator();

    while (allEventsIter.hasNext()) {
        Event e = allEventsIter.next();
        if (eventAttendeeNotMeeting(e, allAttendees, allOptionalAttendees)) {
            allEventsIter.remove();
        }
    }
    return allEvents;
  }

  /** Return `true` if the person attending the event isn't one of the people who wants to book a meeting. */
  private boolean eventAttendeeNotMeeting(Event e, Collection<String> allAttendees, Collection<String> allOptionalAttendees) {
    Set<String> attendees = e.getAttendees();
    
    for (String s : attendees) {
        if ( !( allAttendees.contains(s) || allOptionalAttendees.contains(s)) ) {
            return true;
        }
    }
    return false;
  }

  /** Generate list of events that excludes optional attendees. */
  private List<Event> getEventsWithoutOptionalAttendees(List<Event> allEvents, Collection<String> allOptionalAttendees) {
    List<Event> eventsWithoutOptionalAttendees = new ArrayList(allEvents);
    Iterator<Event> optionalEventsIter = eventsWithoutOptionalAttendees.iterator();
    
    while(optionalEventsIter.hasNext()) {
        Event e = optionalEventsIter.next();
        for (String s : e.getAttendees()) {
            if (allOptionalAttendees.contains(s)) {
                optionalEventsIter.remove();
                break;
            }
        }
    }
    return eventsWithoutOptionalAttendees;
  }

  /** Find all gaps in a given collection of event times. */
  private Collection<TimeRange> findGaps(List<Event> events, long meetingTime, int numAttendees) {
    // Track latest end time of any event in case the event with
    // the latest end time comes before the last event in the event List.
    //
    // Example: The last free gap for events ([5, 20] , [10, 15])
    // should be [20, END_OF_DAY], not [15, END_OF_DAY].
    int endOfLatestEvent = 0;

    TimeRange gap;
    Collection<TimeRange> results = new ArrayList<>();

    // If no one wants to meet, we don't need to find the gaps in their schedules.
    if (numAttendees == 0) {
        return results;
    }

    for (int i = 0; i < events.size(); i++) {        
        if (i == 0) {
            // First gap (before the earliest event).
            gap = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, events.get(i).getWhen().start(), false);
        } else {
            // Gaps inbetween events.
            gap = TimeRange.fromStartEnd(events.get(i-1).getWhen().end(), events.get(i).getWhen().start(), false);
        }

        // Validate gap.
        if (isValidGap(gap, meetingTime, endOfLatestEvent)) {
            results.add(gap);
        }

        // Update latest end time of any event.
        if (i != events.size()) {
            endOfLatestEvent = endOfLatestEvent > events.get(i).getWhen().end() ? endOfLatestEvent : events.get(i).getWhen().end();
        }
    }

    // Last gap (after the end of the latest event).
    gap = TimeRange.fromStartEnd(endOfLatestEvent, TimeRange.END_OF_DAY, true);
    if (isValidGap(gap, meetingTime, endOfLatestEvent)) {
        results.add(gap);
    }
    return results;
  }

  /** Return `true` if a gap is "well-constructed." */
  private boolean isValidGap(TimeRange gap, long meetingTime, int endOfLatestEvent) {
    return gap.start() < gap.end()
        && gap.duration() >= meetingTime
        && gap.start() >= endOfLatestEvent;
    }
}
