package no.nav.klage.dokument.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

/*
* Copyright 2021 Simple JSON Patch contributors
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/ /**
 * A simple (one class) implementation of
 * [RFC 6902 JSON Patch](https://tools.ietf.org/html/rfc6902) using Jackson.
 *
 *
 * This class just applies a patch to a JSON document, nothing fancy like diffs
 * or patch generation.
 *
 */
object JsonPatch {
    /**
     * Applies all JSON patch operations to a JSON document.
     *
     * @return the patched JSON document
     */
    fun apply(patch: ArrayNode, source: JsonNode): JsonNode {
        if (!source.isContainerNode) {
            throw IllegalArgumentException(
                "Invalid JSON document, "
                        + "an object or array is required"
            )
        }
        var result = source.deepCopy<JsonNode>()
        if (patch.size() == 0) {
            return result
        }
        for (operation: JsonNode in patch) {
            if (!operation.isObject) {
                throw IllegalArgumentException("Invalid operation: $operation")
            }
            result = perform(operation as ObjectNode, result)
        }
        return result
    }

    /**
     * Perform one JSON patch operation
     *
     * @return the patched JSON document
     */
    internal fun perform(operation: ObjectNode, doc: JsonNode): JsonNode {
        val opNode = operation["op"]
        if (opNode == null || !opNode.isTextual) {
            throw IllegalArgumentException("Invalid \"op\" property: $opNode")
        }
        val op = opNode.asText()
        val pathNode = operation["path"]
        if (pathNode == null || !pathNode.isTextual) {
            throw IllegalArgumentException("Invalid \"path\" property: $pathNode")
        }
        val path = pathNode.asText()
        if (path.length != 0 && path[0] != '/') {
            throw IllegalArgumentException("Invalid \"path\" property: $path")
        }
        when (op) {
            "add" -> {
                val value = operation.get("value") ?: throw IllegalArgumentException("Missing \"value\" property")
                return add(doc, path, value)
            }
            "remove" -> {
                return remove(doc, path)
            }
            "replace" -> {
                val value = operation.get("value") ?: throw IllegalArgumentException("Missing \"value\" property")
                return replace(doc, path, value)
            }
            "move" -> {
                val fromNode = operation["from"]
                if (fromNode == null || !fromNode.isTextual) {
                    throw IllegalArgumentException("Invalid \"from\" property: $fromNode")
                }
                val from = fromNode.asText()
                if (from.length != 0 && from[0] != '/') {
                    throw IllegalArgumentException("Invalid \"from\" property: $fromNode")
                }
                return move(doc, path, from)
            }
            "copy" -> {
                val fromNode = operation["from"]
                if (fromNode == null || !fromNode.isTextual) {
                    throw IllegalArgumentException("Invalid \"from\" property: $fromNode")
                }
                val from = fromNode.asText()
                if (from.length != 0 && from[0] != '/') {
                    throw IllegalArgumentException("Invalid \"from\" property: $fromNode")
                }
                return copy(doc, path, from)
            }
            "test" -> {
                val value = operation.get("value") ?: throw IllegalArgumentException("Missing \"value\" property")
                return test(doc, path, value)
            }
            else -> throw IllegalArgumentException("Invalid \"op\" property: $op")
        }
    }

    /**
     * Perform a JSON patch "add" operation on a JSON document
     *
     * @return the patched JSON document
     */
    internal fun add(doc: JsonNode, path: String, value: JsonNode): JsonNode {
        if (path.isEmpty()) {
            return value
        }

        // get the path parent
        var parent: JsonNode? = null
        val lastPathIndex = path.lastIndexOf('/')
        if (lastPathIndex < 1) {
            parent = doc
        } else {
            val parentPath = path.substring(0, lastPathIndex)
            parent = doc.at(parentPath)
        }

        // adding to an object
        if (parent!!.isObject) {
            val parentObject = parent as ObjectNode?
            val key = path.substring(lastPathIndex + 1)
            parentObject!!.set<JsonNode>(key, value)
        } else if (parent.isArray) {
            val key = path.substring(lastPathIndex + 1)
            val parentArray = parent as ArrayNode?
            if ((key == "-")) {
                parentArray!!.add(value)
            } else {
                try {
                    val idx = key.toInt()
                    if (idx > parentArray!!.size() || idx < 0) {
                        throw IllegalArgumentException("Array index is out of bounds: $idx")
                    }
                    parentArray.insert(idx, value)
                } catch (e: NumberFormatException) {
                    throw IllegalArgumentException("Invalid array index: $key")
                }
            }
        } else {
            throw IllegalArgumentException("Invalid \"path\" property: $path")
        }
        return doc
    }

    /**
     * Perform a JSON patch "remove" operation on a JSON document
     *
     * @return the patched JSON document
     */
    internal fun remove(doc: JsonNode, path: String): JsonNode {
        if ((path == "")) {
            if (doc.isObject) {
                val docObject = doc as ObjectNode
                docObject.removeAll()
                return doc
            } else if (doc.isArray) {
                val docArray = doc as ArrayNode
                docArray.removeAll()
                return doc
            }
        }

        // get the path parent
        var parent: JsonNode? = null
        val lastPathIndex = path.lastIndexOf('/')
        if (lastPathIndex == 0) {
            parent = doc
        } else {
            val parentPath = path.substring(0, lastPathIndex)
            parent = doc.at(parentPath)
            if (parent.isMissingNode()) {
                throw IllegalArgumentException("Path does not exist: $path")
            }
        }

        // removing from an object
        val key = path.substring(lastPathIndex + 1)
        if (parent!!.isObject) {
            val parentObject = parent as ObjectNode?
            if (!parent.has(key)) {
                throw IllegalArgumentException("Property does not exist: $key")
            }
            parentObject!!.remove(key)
        } else if (parent.isArray) {
            try {
                val parentArray = parent as ArrayNode?
                val idx = key.toInt()
                if (!parent.has(idx)) {
                    throw IllegalArgumentException("Index does not exist: $key")
                }
                parentArray!!.remove(idx)
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Invalid array index: $key")
            }
        } else {
            throw IllegalArgumentException("Invalid \"path\" property: $path")
        }
        return doc
    }

    /**
     * Perform a JSON patch "replace" operation on a JSON document
     *
     * @return the patched JSON document
     */
    internal fun replace(doc: JsonNode, path: String, value: JsonNode): JsonNode {
        var doc = doc
        doc = remove(doc, path)
        return add(doc, path, value)
    }

    /**
     * Perform a JSON patch "move" operation on a JSON document
     *
     * @return the patched JSON document
     */
    internal fun move(doc: JsonNode, path: String, from: String): JsonNode {
        // get the value
        var doc = doc
        val value = doc.at(from)
        if (value.isMissingNode) {
            throw IllegalArgumentException("Invalid \"from\" property: $from")
        }

        // do remove and then add
        doc = remove(doc, from)
        return add(doc, path, value)
    }

    /**
     * Perform a JSON patch "copy" operation on a JSON document
     *
     * @return the patched JSON document
     */
    internal fun copy(doc: JsonNode, path: String, from: String): JsonNode {
        // get the value
        val value = doc.at(from)
        if (value.isMissingNode) {
            throw IllegalArgumentException("Invalid \"from\" property: $from")
        }

        // do add
        return add(doc, path, value)
    }

    /**
     * Perform a JSON patch "test" operation on a JSON document
     *
     * @return the patched JSON document
     */
    internal fun test(doc: JsonNode, path: String, value: JsonNode): JsonNode {
        val node = doc.at(path)
        if (node.isMissingNode) {
            throw IllegalArgumentException("Invalid \"path\" property: $path")
        }
        if (node != value) {
            throw IllegalArgumentException("The value does not equal path node")
        }
        return doc
    }
}