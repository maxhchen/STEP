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
    Collection<String> allAttendees = request.getAttendees();
    int numAttendees = allAttendees.size();
    long meetingTime = request.getDuration();

    // Base Cases.
    if (numAttendees == 0) {
        results = Arrays.asList(TimeRange.WHOLE_DAY);
        return results;
    } else if (meetingTime == 0 || meetingTime > TimeRange.WHOLE_DAY.duration()) {
        results = Arrays.asList();
        return results;
    }

    // Extract and sort event times (i.e. busy times) by start time.
    List<TimeRange> timeRanges = new ArrayList<>();
    for (Event e : events) {

        // Ignore event times of people not actually attending.
        Set<String> attendees = e.getAttendees();
        boolean isOptional = false;
        for (String s : attendees) {
            if (! allAttendees.contains(s)) {
                isOptional = true;
                break;
            }
        }

        if (!isOptional) {
            timeRanges.add(e.getWhen());
        }
    }

    timeRanges.sort(TimeRange.ORDER_BY_START);

    // Track latest end time of any event.
    // Since timeRanges was sorted by start, the event with the latest end time
    // might come before the last event in the List (e.g. if we had [5, 20]
    // followed by [10, 15], we want to use [20, END_OF_DAY] for the final gap,
    // not [15, END_OF_DAY]).
    int endOfLatestEvent = 0;
    TimeRange gap;

    // Iteratively construct gaps between busy times.
    for (int i = 0; i < timeRanges.size(); i++) {        
        // First gap before the first time anyone is busy.
        if (i == 0) {
            gap = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, timeRanges.get(i).start(), false);
        // gaps in the middle.
        } else {
            gap = TimeRange.fromStartEnd(timeRanges.get(i-1).end(), timeRanges.get(i).start(), false);
        }

        // Prevent edge behavior since start times are monotonically increasing,
        // but end times are not.
        if (gap.start() < gap.end() && gap.start() != gap.end() && gap.duration() >= meetingTime ) {
            results.add(gap);                
        }

        // Update latest end time of any event.
        if (i != timeRanges.size()) {
            endOfLatestEvent = endOfLatestEvent > timeRanges.get(i).end() ? endOfLatestEvent : timeRanges.get(i).end();
        }
    }
    // Last gap after the last person is busy.
    gap = TimeRange.fromStartEnd(endOfLatestEvent, TimeRange.END_OF_DAY, true);
    if (gap.start() < gap.end()) {
        results.add(gap);
    }

    return results;

  }
}
