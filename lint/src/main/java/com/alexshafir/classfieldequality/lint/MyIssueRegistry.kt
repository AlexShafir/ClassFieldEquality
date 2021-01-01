package com.alexshafir.classfieldequality.lint

import com.android.tools.lint.client.api.IssueRegistry

class MyIssueRegistry : IssueRegistry() {
    override val api: Int = com.android.tools.lint.detector.api.CURRENT_API

    override val issues = listOf(
        issueEquality
    )
}