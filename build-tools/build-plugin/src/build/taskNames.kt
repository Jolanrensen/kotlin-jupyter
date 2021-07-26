package build

const val LOCAL_INSTALL_GROUP = "local install"
const val DISTRIBUTION_GROUP = "distrib"
const val CONDA_GROUP = "conda"
const val PYPI_GROUP = "pip"
const val BUILD_GROUP = "build"
const val VERIFICATION_GROUP = "verification"
const val PUBLISHING_GROUP = "publishing"

const val CHECK_TASK = "check"
const val CHECK_README_TASK = "checkReadme"
const val GENERATE_README_TASK = "generateReadme"

const val BUILD_PROPERTIES_TASK = "buildProperties"
const val CONDA_PACKAGE_TASK = "condaPackage"
const val PYPI_PACKAGE_TASK = "pyPiPackage"
const val PROCESS_RESOURCES_TASK = "processResources"

const val JAR_TASK = "jar"
const val SHADOW_JAR_TASK = "shadowJar"

const val PUBLISH_LOCAL_TASK = "publishLocal"

const val INSTALL_COMMON_REQUIREMENTS_TASK = "installCommonRequirements"
const val INSTALL_HINT_REMOVER_REQUIREMENTS_TASK = "installHintRemoverRequirements"
const val COPY_DISTRIB_FILES_TASK = "copyDistribFiles"
const val PREPARE_DISTRIBUTION_DIR_TASK = "prepareDistributionDir"

const val PUSH_CHANGES_TASK = "pushChanges"
const val UPDATE_LIBRARY_PARAM_TASK = "updateLibraryParam"
const val UPDATE_KOTLIN_VERSION_TASK = "updateKotlinVersion"
const val COPY_NB_EXTENSION_TASK = "copyNbExtension"
const val COPY_RUN_KERNEL_PY_TASK = "copyRunKernelPy"
const val UNINSTALL_TASK = "uninstall"

const val MAKE_CHANGES_PR_TASK = "makeChangesPR"
const val PREPARE_PACKAGE_TASK = "preparePackage"

const val UPDATE_LIBRARIES_TASK_NAME = "updateLibraryDescriptors"
