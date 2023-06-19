package io.jenkins.plugins.sample

import hudson.model.FreeStyleBuild
import hudson.model.FreeStyleProject
import hudson.model.Label
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.job.WorkflowRun
import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule

class HelloWorldBuilderTest {
    @get:Rule
    val jenkins = JenkinsRule()

    val name = "Bobby"

    @Test
    fun testConfigRoundtrip() {
        var project: FreeStyleProject = jenkins.createFreeStyleProject()
        project.buildersList.add(HelloWorldBuilder(name))
        project = jenkins.configRoundtrip(project)
        jenkins.assertEqualDataBoundBeans(HelloWorldBuilder(name), project.buildersList[0])
    }

    @Test
    fun testConfigRoundtripFrench() {
        var project: FreeStyleProject = jenkins.createFreeStyleProject()
        val builder = HelloWorldBuilder(name)
        builder.setUseFrench(true)
        project.buildersList.add(builder)
        project = jenkins.configRoundtrip(project)

        val lhs = HelloWorldBuilder(name)
        lhs.setUseFrench(true)
        jenkins.assertEqualDataBoundBeans(lhs, project.buildersList[0])
    }

    @Test
    fun testBuild() {
        val project: FreeStyleProject = jenkins.createFreeStyleProject()
        val builder = HelloWorldBuilder(name)
        project.buildersList.add(builder)

        val build: FreeStyleBuild = jenkins.buildAndAssertSuccess(project)
        jenkins.assertLogContains("Hello, $name", build)
    }

    @Test
    fun testBuildFrench() {
        val project: FreeStyleProject = jenkins.createFreeStyleProject()
        val builder = HelloWorldBuilder(name)
        builder.setUseFrench(true)
        project.buildersList.add(builder)

        val build: FreeStyleBuild = jenkins.buildAndAssertSuccess(project)
        jenkins.assertLogContains("Bonjour, $name", build)
    }

    @Test
    fun testScriptedPipeline() {
        val agentLabel = "my-agent"
        jenkins.createOnlineSlave(Label.get(agentLabel))
        val job: WorkflowJob = jenkins.createProject(WorkflowJob::class.java, "test-scripted-pipeline")
        val pipelineScript = """
            node {
                greet '$name'
            }
            """
        job.definition = CpsFlowDefinition(pipelineScript, true)
        val completedBuild: WorkflowRun = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0))
        val expectedString = "Hello, $name!"
        jenkins.assertLogContains(expectedString, completedBuild)
    }
}
