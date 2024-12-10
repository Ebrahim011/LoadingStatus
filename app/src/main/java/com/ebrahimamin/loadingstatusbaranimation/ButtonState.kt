package com.ebrahimamin.loadingstatusbaranimation

sealed class ButtonState {
    object Clicked : ButtonState()
    object Loading : ButtonState()
    object Completed : ButtonState()
}