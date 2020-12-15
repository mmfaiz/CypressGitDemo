package com.matchi.requests

import com.matchi.User
import com.matchi.activities.trainingplanner.Trainer

class TrainerRequest extends Request {

    Trainer trainer

    Date start
    Date end

    static constraints = {
        trainer nullable: false
        start nullable: false
        end nullable: false
    }

    static TrainerRequest create(Long requesterId, Long trainerId, Date start, Date end) {
        User requester  = User.get(requesterId)
        Trainer trainer = Trainer.get(trainerId)

        TrainerRequest request = new TrainerRequest()
        request.requester = requester
        request.trainer   = trainer
        request.start     = start
        request.end       = end

        if (!request.hasErrors() && request.save()) {
            return request
        }
    }
}
