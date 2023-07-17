package com.zegreatrob.testmints.action.annotation

annotation class ActionMint

@Deprecated(message = "Use ActionMint instead.", replaceWith = ReplaceWith("ActionMint"))
typealias MintAction = ActionMint
