package com.EventCrafters.EventCrafters.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventLiveStatsDTO {
    private boolean hasUserJoined;
    private boolean hasUserReviewed;
    private boolean eventFinished;

    public EventLiveStatsDTO() {
    }

}
