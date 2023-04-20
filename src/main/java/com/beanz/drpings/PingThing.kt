package com.beanz.drpings

class PingThing(var name: String, var x: Int, var y: Int, var z: Int) {
    fun `is`(name: String): Boolean {
        return name.equals(this.name, ignoreCase = true)
    }
}