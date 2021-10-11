package ru.skillbranch.skillarticles.ui.custom.markdown

import java.lang.StringBuilder
import java.util.regex.Pattern

object MarkdownParser {

    private val LINE_SEPARATOR = System.getProperty("line.separator") ?: "\n"

    //group regex
    private const val UNORDERED_LIST_ITEM_GROUP = "(^[*+-] .+$)"
    private const val ORDERED_LIST_ITEM_GROUP = "(^\\d. .+$)"
    private const val HEADER_GROUP = "(^#{1,6} .+?$)"
    private const val QUOTE_GROUP = "(^> .+?$)"
    private const val ITALIC_GROUP = "((?<!\\*)\\*[^*].*?[^*]?\\*(?!\\*)|(?<!_)_[^_].*?[^_]?_(?!_))"
    private const val BOLD_GROUP =
        "((?<!\\*)\\*{2}[^*].*?[^*]?\\*{2}(?!\\*)|(?<!\\_)\\_{2}[^_].*?[^_]?\\_{2}(?!\\_))"
    private const val STRIKE_GROUP = "((?<!\\~)\\~{2}[^~].*?[^~]?\\~{2}(?!\\~))"
    private const val RULE_GROUP = "(^[-_*]{3}$)"
    private const val INLINE_GROUP = "((?<!`)`[^`\\s].*?[^`\\s]?`(?!`))"
    private const val LINK_GROUP = "(\\[[^\\[\\]]*?]\\(.+?\\)|^\\[*?]\\(.*?\\))"
    private const val BLOCK_CODE_GROUP =
        "((?<!\\`{3})\\`{3}[^\\`{3}].*?[^\\`{3}]*?\\S\\`{3}(?!\\`{3}))"
    private const val IMAGE_GROUP = "(!\\[[^\\[\\]]*?]\\(.+?\\)|^\\[*?]\\(.*?\\))"


    //result regex
    const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM_GROUP|$HEADER_GROUP|$QUOTE_GROUP|" +
            "$ITALIC_GROUP|$BOLD_GROUP|$STRIKE_GROUP|$RULE_GROUP|$INLINE_GROUP|$LINK_GROUP|" +
            "$ORDERED_LIST_ITEM_GROUP|$BLOCK_CODE_GROUP|$IMAGE_GROUP"

    private val elementsPattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }

    /**
     * parse markdown text to elements
     */

    fun parse(string: String): MarkdownText {
        val elements = mutableListOf<Element>()
        elements.addAll(findElements(string))
        return MarkdownText(elements)
    }

    /**
     * clear markdown text to string without markdown characters
     */

    fun clear(string: String): String? {
        val result = parse(string)
        val builder = StringBuilder()
        result.elements.forEach {
            clearChildren(it, builder)
        }
        return builder.toString()
    }

    private fun clearChildren(element: Element, builder: StringBuilder): CharSequence {
        return builder.apply {
            when (element) {
                is Element.Text -> append(element.text)

                is Element.UnorderedListItem -> {
                    for (child in element.elements) {
                        clearChildren(child, builder)
                    }
                }

                is Element.Quote -> {
                    for (child in element.elements) {
                        clearChildren(child, builder)
                    }
                }

                is Element.Header -> append(element.text)

                is Element.Italic, is Element.Bold, is Element.Strike -> {
                    for (child in element.elements) {
                        clearChildren(child, builder)
                    }
                }

                is Element.Rule, is Element.InlineCode, is Element.Link -> append(element.text)

                else -> append(element.text)
            }
        }
    }


    /**
     *  find markdown elements in markdown text
     */

    private fun findElements(string: CharSequence): List<Element> {
        val parents = mutableListOf<Element>()
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0

        loop@ while (matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()

            if (lastStartIndex < startIndex) {
                parents.add(Element.Text(string.subSequence(lastStartIndex, startIndex)))
            }

            var text: CharSequence

            val groups = 1..12
            var group = -1
            for (gr in groups) {
                if (matcher.group(gr) != null) {
                    group = gr
                    break
                }
            }

            when (group) {
                -1 -> break@loop

                // UNORDERED_GROUP
                1 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex)

                    val subs = findElements(text)
                    val element = Element.UnorderedListItem(text, subs)
                    parents.add(element)

                    lastStartIndex = endIndex
                }

                //HEADERS
                2 -> {
                    val reg = "^#{1,6}".toRegex().find(string.subSequence(startIndex, endIndex))
                    val level = reg!!.value.length

                    text = string.subSequence(startIndex.plus(level.inc()), endIndex)

                    val element = Element.Header(level, text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //QUOTE
                3 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex)
                    val subElements = findElements(text)
                    val element = Element.Quote(text, subElements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //INTALIC
                4 -> {
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val subElements = findElements(text)
                    val element = Element.Italic(text, subElements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //BOLD
                5 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex.minus(2))
                    val subElements = findElements(text)
                    val element = Element.Bold(text, subElements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //STRIKE
                6 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex.minus(2))
                    val subElements = findElements(text)
                    val element = Element.Strike(text, subElements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //RULE
                7 -> {
                    val element = Element.Rule()
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //CODE
                8 -> {
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val element = Element.InlineCode(text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //LINK
                9 -> {
                    text = string.subSequence(startIndex, endIndex)
                    val (title: String, link: String) = "\\[(.*)]\\((.*)\\)".toRegex()
                        .find(text)!!.destructured
                    val element = Element.Link(link, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //ORDERED_LINK
                10 -> {
                    val reg = "^\\d.".toRegex().find(string.subSequence(startIndex, endIndex))
                    val valueLength = reg!!.value.length
                    val order = reg.value
                    text = string.subSequence(startIndex.plus(valueLength.inc()), endIndex)
                    val subElements = findElements(text)
                    val element = Element.OrderedListItem(order, text, subElements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //BLOCKED_CODE
                11 -> {
                    text = string.subSequence(startIndex.plus(3), endIndex.minus(3))
                    val subElements = findElements(text)
                    val element = Element.BlockCode(text, subElements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //IMAGES
                12 -> {
                    text = string.subSequence(startIndex, endIndex)
                    val (alt: String, link: String) = "\\[(.*)]\\((.*)\\)".toRegex()
                        .find(text)!!.destructured

                    val element = if (link.contains("\".*?\"".toRegex())) {
                        val (newLink: String, title: String) = "(.*)(\".*?\")".toRegex()
                            .find(link)!!.destructured
                        Element.Image(
                            newLink.subSequence(0, newLink.lastIndex).toString(),
                            if (alt.isEmpty()) null else alt,
                            title.subSequence(1, title.lastIndex).toString()
                        )
                    } else {
                        Element.Image(link, if (alt.isEmpty()) null else alt, "")
                    }

                    parents.add(element)
                    lastStartIndex = endIndex
                }
            }
        }

        if (lastStartIndex < string.length) {
            val text = string.subSequence(lastStartIndex, string.length)
            parents.add(Element.Text(text))
        }

        return parents
    }
}

data class MarkdownText(val elements: List<Element>)

sealed class Element {
    abstract val text: CharSequence
    abstract val elements: List<Element>

    data class Text(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class UnorderedListItem(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Header(
        val level: Int = 1,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Quote(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Italic(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Bold(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Strike(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Rule(
        override val text: CharSequence = "",
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class InlineCode(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Link(
        val link: String,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class OrderedListItem(
        val order: String,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class BlockCode(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Image(
        val url: String,
        val alt: String?,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()
}