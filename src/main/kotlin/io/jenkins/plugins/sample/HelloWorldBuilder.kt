package io.jenkins.plugins.sample

import hudson.EnvVars
import hudson.Extension
import hudson.FilePath
import hudson.Launcher
import hudson.model.AbstractProject
import hudson.model.Run
import hudson.model.TaskListener
import hudson.tasks.BuildStepDescriptor
import hudson.tasks.Builder
import hudson.util.FormValidation
import jenkins.tasks.SimpleBuildStep
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import org.kohsuke.stapler.QueryParameter
import java.io.IOException
import javax.servlet.ServletException

class HelloWorldBuilder @DataBoundConstructor constructor(private val name: String) : Builder(), SimpleBuildStep {
    private var useFrench: Boolean = false

    fun getName(): String {
        return name
    }

    fun isUseFrench(): Boolean {
        return useFrench
    }

    @DataBoundSetter
    fun setUseFrench(useFrench: Boolean) {
        this.useFrench = useFrench
    }

    @Throws(InterruptedException::class, IOException::class)
    override fun perform(
        run: Run<*, *>, workspace: FilePath, env: EnvVars, launcher: Launcher, listener: TaskListener
    ) {
        if (useFrench) {
            listener.logger.println("Bonjour, $name!")
        } else {
            listener.logger.println("Hello, $name!")
        }
    }

    @Symbol("greet")
    @Extension
    class DescriptorImpl : BuildStepDescriptor<Builder>() {
        @Throws(IOException::class, ServletException::class)
        fun doCheckName(
            @QueryParameter value: String, @QueryParameter useFrench: Boolean
        ): FormValidation {
            if (value.isEmpty()) {
                return FormValidation.error(Messages.HelloWorldBuilder_DescriptorImpl_errors_missingName())
            }
            if (value.length < 4) {
                return FormValidation.warning(Messages.HelloWorldBuilder_DescriptorImpl_warnings_tooShort())
            }
            if (!useFrench && value.matches(".*[éáàç].*".toRegex())) {
                return FormValidation.warning(Messages.HelloWorldBuilder_DescriptorImpl_warnings_reallyFrench())
            }
            return FormValidation.ok()
        }

        override fun isApplicable(aClass: Class<out AbstractProject<*, *>>) = true

        override fun getDisplayName(): String = Messages.HelloWorldBuilder_DescriptorImpl_DisplayName()
    }
}
