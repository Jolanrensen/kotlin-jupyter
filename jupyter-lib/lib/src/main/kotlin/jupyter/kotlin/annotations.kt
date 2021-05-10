package jupyter.kotlin

/**
 * Describes the dependency
 *
 * @property value Can be one of the following:
 * - Maven artifact coordinates in the following form:
 * `<groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}`
 * - Path to the JAR file (absolute or relative to the directory specified in [Repository])
 */
@Target(AnnotationTarget.FILE)
@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class DependsOn(val value: String = "")

/**
 * Describes the repository which is used for dependency resolution
 *
 * @property value Can be one of the following:
 * - Maven repository URL
 * - Local directory in which JARs are stored
 */
@Target(AnnotationTarget.FILE)
@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class Repository(val value: String = "")

/**
 * Describes compilation arguments used for the compilation of this and all following snippets
 *
 * @property values List of free compiler arguments
 */
@Target(AnnotationTarget.FILE)
@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class CompilerArgs(vararg val values: String)
