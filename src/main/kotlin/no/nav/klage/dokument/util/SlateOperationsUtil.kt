package no.nav.klage.dokument.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun apply(json: String, operation: SlateOperation): String {
//    val engine = com.oracle.truffle.js.scriptengine.GraalJSEngineFactory().scriptEngine
//    val invocable = Files.newBufferedReader(Paths.get("src/main/resources/slate/slate.js")).createInvocable(engine)
//    val v = invocable.invokeFunction("hello", "Kalle", "Anka")//with or without arguments
//    println("js returned $v")

    val node = jacksonObjectMapper().readTree(json)

    val o = when (operation.type) {
        "move_node" -> JsonPatchOperation(
            op = operation.type.toJsonPatchType(),
            path = operation.newPath.fromSlatePathToJsonPath(node),
            from = operation.path.fromSlatePathToJsonPath(node),
        )
        "remove_node" -> JsonPatchOperation(
            op = operation.type.toJsonPatchType(),
            path = operation.path.fromSlatePathToJsonPath(node),
        )
        "remove_text" -> JsonPatchOperation(
            op = operation.type.toJsonPatchType(),
            path = operation.path.fromSlatePathToJsonPath(node),
            text = operation.text,
            offset = operation.offset,
        )
        "set_node" -> JsonPatchOperation(
            op = operation.type.toJsonPatchType(),
            path = operation.path.fromSlatePathToJsonPath(node),
            text = operation.text,
            offset = operation.offset,
            properties = operation.properties,
            newProperties = operation.newProperties,
        )
        "split_node" -> JsonPatchOperation(
            op = operation.type.toJsonPatchType(),
            path = operation.path.fromSlatePathToJsonPath(node),
            position = operation.position,
            properties = operation.properties,
        )
        else -> error("wrong type: ${operation.type}")
    }

    val jsonPatchOperationAsJson = jacksonObjectMapper().writeValueAsString(listOf(o))

    val resultingJsonNode =
        JsonPatch.apply(patch = jacksonObjectMapper().readTree(jsonPatchOperationAsJson) as ArrayNode, source = node)

    return resultingJsonNode.toPrettyString()
}

private fun String.toJsonPatchType(): String {
    return when (this) {
        "move_node" -> "move"
        "remove_node" -> "remove"
        "remove_text" -> "remove_text"
        "set_node" -> "set_node"
        "split_node" -> "split_node"
        else -> error("bad input: $this")
    }
}

private fun String?.fromSlatePathToJsonPath(rootNode: JsonNode): String {
    if (this == null) {
        return "/"
    }

    var jsonPath = "/"

    var currentNode: JsonNode = rootNode

    this.substring(1, this.lastIndex).split(", ").map { it.toInt() }.forEach {
//        if (Text.isText(rootNode) || !rootNode.children[p]) {
//            throw new Error("Cannot find a descendant at path [".concat(path, "] in node: ").concat(JSON.stringify(root)));
//        }

        if (!currentNode.has("children")) {
            error("no children node")
        }

        currentNode = currentNode["children"].get(it)
        jsonPath += "children/$it/"

    }

    //remove last /
    if (jsonPath.length > 2) {
        jsonPath = jsonPath.substring(0, jsonPath.lastIndex)
    }

    return jsonPath
}


data class SlateOperation(
    val type: String,
    val path: String? = null,
    val newPath: String? = null,
    val text: String? = null,
    val offset: Int? = null,
    val properties: Map<String, String>? = null,
    val newProperties: Map<String, String>? = null,
    val position: Int? = null,
)

data class JsonPatchOperation(
    val op: String,
    val path: String? = null,
    val value: String? = null,
    val from: String? = null,
    val text: String? = null,
    val offset: Int? = null,
    val properties: Map<String, String>? = null,
    val newProperties: Map<String, String>? = null,
    val position: Int? = null,
)

//fun String.createInvocable(engine: ScriptEngine): Invocable {
//    engine.eval(this);
//    return engine as Invocable
//}
//
//fun Reader.createInvocable(engine: ScriptEngine): Invocable {
//    engine.eval(this)
//    return engine as Invocable
//}


