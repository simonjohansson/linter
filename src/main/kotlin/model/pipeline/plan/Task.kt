package model.pipeline.plan

data class Source(val repository: String = "")
data class ImageResource(val type: String = "docker-image", val source: Source = Source())
data class Run(val path: String = "", val dir: String = "")
data class Input(val name: String = "")
data class Config(
        val platform: String = "linux",
        val image_resource: ImageResource = ImageResource(),
        val run: Run = Run(),
        val inputs: List<Input> = listOf(Input())
)

data class Task(val task: String = "", val config: Config = Config()): PlanItem
