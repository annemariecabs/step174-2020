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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/*
 * A class to find a time that satisfies a meeting query and ensures
 * all mandatory attendees can attend and the most possible
 * optional attendees can attend.
 */
public final class FindMeetingQuery {
   
  /** the number of minutes in a day */
  private static final int MINUTES_IN_DAY = 24 * 60;

  /**
   * A private class that holds the necessary elements of availability:
   * can all mandatory attendees attend and how many optional attendees can't come.
   */
  private class Availability {
    /** holds whether mandatory attendees are available at this time */
    private boolean areMandatoryAttendeesAllAvailable;
    /** holds the number of optional attendees that can't attend */
    private int numberOfOptionalAttendeesUnavailable;

    /**
     * Constructs the default type of Availability which is that everyone can attend 
     * hence areMandatoryAttendeesAllAvailable is true and number of optional attendees
     * that are unavailable is 0.
     */
    public Availability() {
      areMandatoryAttendeesAllAvailable = true;
      numberOfOptionalAttendeesUnavailable = 0;
    }

    /**
     * If any one (or multiple) of the mandatory attendees can't make it, 
     * sets the mandatory availability of this time to false.
     */
    public void mandatoryAttendeeUnavailable() {
      areMandatoryAttendeesAllAvailable = false;
    }

    /**
     * Increments the number of optional attendees that can't come by given number increase.
     */
    public void increaseOptionalAttendeeUnavailability(int increase) {
      numberOfOptionalAttendeesUnavailable += increase;
    }

    // TODO: remove
    public String toString() {
      return "{" + areMandatoryAttendeesAllAvailable + ", " + numberOfOptionalAttendeesUnavailable +"}";
    }
  }
  
  /**
   * Takes the events of the day and information about a potential meeting 
   * and returns the time ranges in which this meeting could be scheduled
   *
   * @return a collection of TimeRanges in which the meeting could be scheduled
   * @param events the collection of events scheduled for that day
   * @param request the meeting request to be fulfilled (will have duration & attendees)
   */ 
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> attendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();

    // The +1 is to account for last minute in day
    Availability[] minuteAvailability = new Availability[MINUTES_IN_DAY + 1];
    // fill the minute availability array with the default (the assumption all attendees can come)
    Arrays.fill(minuteAvailability, new Availability());

    // keep a champion for the maximum number of unavailable attendees found so that when analyzing the minuteAvailability
    // array, we can determine where to stop. starts at 0 b/c that's the lowest number of unavailable
    // attendees possible
    int maxUnavailableAttendeesFound = 0;

    // fill the minuteAvailability array with the correct availabilities based on events given
    for (Event event: events) {
      Availability status = new Availability();
      int mandatoryOverlap = amountOfOverlap(event.getAttendees(), attendees);

      if (mandatoryOverlap > 0) {
        status.mandatoryAttendeeUnavailable();
      }

      int optionalOverlap = amountOfOverlap(event.getAttendees(), optionalAttendees);
      status.increaseOptionalAttendeeUnavailability(optionalOverlap);
       
      // if status isn't just the default, update the array
      if (!status.areMandatoryAttendeesAllAvailable || status.numberOfOptionalAttendeesUnavailable != 0) {
        TimeRange range = event.getWhen();

        for (int i = range.start(); i < range.end(); i++) {
          // update the mandatory attendee availability of this minute
          if (!status.areMandatoryAttendeesAllAvailable) {
            minuteAvailability[i].mandatoryAttendeeUnavailable();
          }
          
          // update the optional availability of this minute
          minuteAvailability[i].increaseOptionalAttendeeUnavailability(status.numberOfOptionalAttendeesUnavailable);
          
          int currentNumberOfUnavailableAttendees = minuteAvailability[i].numberOfOptionalAttendeesUnavailable;
          
          // if the current number of unavailable attendees is the highest number yet, update the champion
          if (currentNumberOfUnavailableAttendees > maxUnavailableAttendeesFound) {
            maxUnavailableAttendeesFound = currentNumberOfUnavailableAttendees;
          }
        }
      }
    }

    System.out.println(Arrays.toString(minuteAvailability));

    for (int status = 0; status <= maxUnavailableAttendeesFound; status++) {
      // an array list of available times 
      ArrayList<TimeRange> availableTimes = (ArrayList<TimeRange>) findTimesForStatus(request, minuteAvailability, status);

      // as soon as the times are found return it, bc the number of optional attendees who can't come
      // will only increase after this point
      if (availableTimes.size() > 0) return availableTimes;
    }

    // if there are no available times, return an empty array list
    return new ArrayList<TimeRange>();
  }
  
  /**
   * A private helper method to determine the amount of overlap between
   * two String Collections
   *
   * @return the number of elements the two Collections have in common
   */
  private int amountOfOverlap(Collection<String> left, Collection<String> right) {
    int overlap = 0;

    for (String leftElement: left) {
      for (String rightElement: right) {
        if (leftElement.equals(rightElement)) {
          overlap++;
        }
      }
    }

    return overlap;
  }

  /** 
   * A private helper method that locates the times that work for the current status (which 
   * indicates how many optional attendees can make it). It will return whatever time ranges
   * are of a long enough duration.
   * 
   * @return the time ranges that allow this number of optional attendees and are long enough
   */
  private Collection<TimeRange> findTimesForStatus(MeetingRequest request, Availability[] minuteAvailability, int status) {
    ArrayList<TimeRange> availableTimes = new ArrayList<TimeRange>();
    int start = 0;
    boolean wasLastMinuteAvailable = minuteAvailability[0].areMandatoryAttendeesAllAvailable 
        && minuteAvailability[0].numberOfOptionalAttendeesUnavailable <= status;
  
    for (int i = 0; i < minuteAvailability.length; i++) {
    
      // checks if the current minute is available by checking that mandatory attendees are available
      // and the status is same or better than current status
      boolean currentMinuteAvailable = minuteAvailability[i].areMandatoryAttendeesAllAvailable 
        && minuteAvailability[i].numberOfOptionalAttendeesUnavailable <= status;

      if (wasLastMinuteAvailable) {
        // If the previous minute was available, but the current minute is unavailable or if it's
        // the end of the day, this is the end of an available time range. If the time range is longer 
        // than the required duration, it's recorded as an available time range.
        if (! currentMinuteAvailable || i == minuteAvailability.length - 1) {
          int end = i;
          int duration = end - start;

          if (duration >= request.getDuration()) {
            availableTimes.add(TimeRange.fromStartEnd(start, end - 1, true)); // add time range (inclusive of start & end) 
          }
        
          wasLastMinuteAvailable = false;
        }      
      } else {
        // If the last minute was unavailable, but this minute is available, then this is the beginning
        // of a new available time range, so start will be set to this minute and wasLastMinuteAvailable to true.
        if (currentMinuteAvailable) {
          start = i;
          wasLastMinuteAvailable = true;
        }
      }
    }

    return availableTimes;
  }
}
