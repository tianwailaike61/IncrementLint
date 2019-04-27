package com.jianghongkui.lib_client;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.LintOptions;
import com.android.builder.model.Variant;
import com.android.tools.lint.LintCliFlags;
import com.android.tools.lint.Reporter;
import com.android.tools.lint.checks.BuiltinIssueRegistry;
import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.gradle.LintGradleClient;
import com.android.tools.lint.gradle.SyncOptions;
import com.android.tools.lint.gradle.api.LintExecutionRequest;
import com.android.tools.lint.gradle.api.VariantInputs;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

import java.io.File;

/**
 * @author hongkui.jiang
 * @Date 2019-04-25
 */
public class ClientCreator {

    public final static IncrementLintClient create(LintExecutionRequest descriptor) {
        ToolingModelBuilderRegistry toolingRegistry = descriptor.getToolingRegistry();
        AndroidProject modelProject =
                createAndroidProject(descriptor.getProject(), toolingRegistry);
        String variantName = descriptor.getVariantName();
        if (variantName == null) {
            return null;
        }
        Variant readVariant = null;
        for (Variant variant : modelProject.getVariants()) {
            if (variant.getName().equals(variantName)) {
                readVariant = variant;
                break;
            }
        }
        if (readVariant == null) {
            return null;
        }
        VariantInputs variantInputs = descriptor.getVariantInputs(readVariant.getName());
        if (variantInputs == null) {
            return null;
        }
        IssueRegistry registry = new BuiltinIssueRegistry();
        LintCliFlags flags = new LintCliFlags();
        IncrementLintClient client = new IncrementLintClient(
                descriptor.getGradlePluginVersion(),
                registry,
                flags,
                descriptor.getProject(),
                descriptor.getSdkHome(),
                readVariant,
                variantInputs,
                descriptor.getBuildTools(),
                true,
                readVariant.getName());
        boolean fatalOnly = descriptor.isFatalOnly();
        if (fatalOnly) {
            flags.setFatalOnly(true);
        }
        LintOptions lintOptions = descriptor.getLintOptions();
        if (lintOptions != null) {
            syncOptions(
                    lintOptions,
                    client,
                    flags,
                    readVariant,
                    descriptor.getProject(),
                    descriptor.getReportsDir(),
                    true,
                    fatalOnly,
                    true);
        }
        return client;
    }

    private static void syncOptions(
            @Nullable LintOptions options,
            @NonNull LintGradleClient client,
            @NonNull LintCliFlags flags,
            @Nullable Variant variant,
            @NonNull Project project,
            @Nullable File reportsDir,
            boolean report,
            boolean fatalOnly,
            boolean allowAutoFix) {
        if (options != null) {
            SyncOptions.syncTo(
                    options,
                    client,
                    flags,
                    variant != null ? variant.getName() : null,
                    project,
                    reportsDir,
                    report);
        }

        client.syncConfigOptions();

        if (!allowAutoFix && flags.isAutoFix()) {
            flags.setAutoFix(false);
        }

        boolean displayEmpty = !(fatalOnly || flags.isQuiet());
        for (Reporter reporter : flags.getReporters()) {
            reporter.setDisplayEmpty(displayEmpty);
        }
    }

    private static AndroidProject createAndroidProject(Project gradleProject, ToolingModelBuilderRegistry toolingRegistry) {
        String modelName = AndroidProject.class.getName();
        ToolingModelBuilder modelBuilder = toolingRegistry.getBuilder(modelName);
        assert modelBuilder.canBuild(modelName) : modelName;

        final ExtraPropertiesExtension ext = gradleProject.getExtensions().getExtraProperties();
        synchronized (ext) {
            ext.set(
                    AndroidProject.PROPERTY_BUILD_MODEL_ONLY_VERSIONED,
                    Integer.toString(AndroidProject.MODEL_LEVEL_3_VARIANT_OUTPUT_POST_BUILD));
            ext.set(AndroidProject.PROPERTY_BUILD_MODEL_DISABLE_SRC_DOWNLOAD, true);

            try {
                return (AndroidProject) modelBuilder.buildAll(modelName, gradleProject);
            } finally {
                ext.set(AndroidProject.PROPERTY_BUILD_MODEL_ONLY_VERSIONED, null);
                ext.set(AndroidProject.PROPERTY_BUILD_MODEL_DISABLE_SRC_DOWNLOAD, null);
            }
        }
    }
}
