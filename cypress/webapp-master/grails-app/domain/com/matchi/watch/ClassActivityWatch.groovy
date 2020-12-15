package com.matchi.watch

import com.matchi.activities.ClassActivity

/**
 * @author Sergei Shushkevich
 */
class ClassActivityWatch extends ObjectWatch {

    ClassActivity classActivity

    static constraints = {
        classActivity(nullable: true)
    }

    static mapping = {
        discriminator "class_activity_watch"
    }
}