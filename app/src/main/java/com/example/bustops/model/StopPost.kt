package com.example.bustops.model

import com.taskworld.kraph.Kraph


// Querry to HSL DigiTransit https://digitransit.fi
class StopPost(
     val query: Kraph = Kraph{
        query {
            fieldObject("stop",args = mapOf("id" to "HSL:1040129")) {
                field("name")
                field("lat")
                field("lon")
            }
        }
    }
)



