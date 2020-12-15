package com.matchi.viewhelpers

import com.matchi.ActivityService
import com.matchi.CustomerService
import com.matchi.activities.trainingplanner.CourseParticipantService
import com.matchi.courses.EditParticipantService

/**
 * Factory class for different ParticipantViewHelpers
 */
class ParticipantViewHelperFactory {

    /**
     * Factory method, decides interface based on params.actionTitle
     * @param flow
     * @param params
     * @param cmd
     * @return
     */
    static ParticipantViewHelper build(String actionTitle) {
        switch(actionTitle) {
            case EditParticipantService.MOVE_PARTICIPANTS_TITLE:
                return new MoveParticipantViewHelper()
            case EditParticipantService.COPY_PARTICIPANTS_TITLE:
                return new CopyParticipantViewHelper()
            case EditParticipantService.ADD_PARTICIPANTS_TITLE:
                return new AddParticipantViewHelper()
            default:
                return null
        }
    }

}
