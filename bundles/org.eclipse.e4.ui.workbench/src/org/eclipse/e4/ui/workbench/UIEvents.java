/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.workbench;

/**
 * E4 UI events and event topic definitions.
 * 
 * This file contains generated and hand crafted event topic constants. There are also hand crafted
 * utility methods for constructing topic strings and publishing events.
 * 
 * When the UI model changes org.eclipse.e4.ui.internal.workbench.swt.GenTopic should be run as an
 * Eclipse application and the console results should be pasted into this file replacing the code
 * below the "Place Generated Code Here" comment
 */
public class UIEvents {

	/**
	 * Topic separator character
	 */
	public static final String TOPIC_SEP = "/"; //$NON-NLS-1$

	/**
	 * Wild card character for matching all sub topics
	 */
	public static final String ALL_SUB_TOPICS = "*"; //$NON-NLS-1$

	/**
	 * Base name of all E4 UI events
	 */
	public static final String UITopicBase = "org/eclipse/e4/ui"; //$NON-NLS-1$

	/**
	 * Name element for all E4 UI model events (these are generated by GenTopic)
	 */
	public static final String UIModelTopicBase = UITopicBase + "/model"; //$NON-NLS-1$

	/**
	 * E4 UI Event Types
	 */
	public static interface EventTypes {
		/**
		 * Creation event
		 */
		public static final String CREATE = "CREATE"; //$NON-NLS-1$
		/**
		 * Set event
		 */
		public static final String SET = "SET"; //$NON-NLS-1$
		/**
		 * Add event
		 */
		public static final String ADD = "ADD"; //$NON-NLS-1$
		/**
		 * Remove event
		 */
		public static final String REMOVE = "REMOVE"; //$NON-NLS-1$
	}

	/**
	 * E4 UI Event argument attribute keys. These are used as keys for the event arguments map. Each
	 * event may have none, some, or all arguments set.
	 */
	public static interface EventTags {
		/**
		 * The element that caused the event to be published
		 */
		public static final String ELEMENT = "ChangedElement"; //$NON-NLS-1$
		/**
		 * The widget that generated the event
		 */
		public static final String WIDGET = "Widget"; //$NON-NLS-1$
		/**
		 * The event type @see UIEvents.EventTypes
		 */
		public static final String TYPE = "EventType"; //$NON-NLS-1$
		/**
		 * The attribute name
		 */
		public static final String ATTNAME = "AttName"; //$NON-NLS-1$
		/**
		 * The old value
		 */
		public static final String OLD_VALUE = "OldValue"; //$NON-NLS-1$
		/**
		 * The new value
		 */
		public static final String NEW_VALUE = "NewValue"; //$NON-NLS-1$
	}

	/**
	 * E4 UI life cycle events. These events are explicitly published by specific operations. They
	 * are not directly generated by UI model changes.
	 */
	public static interface UILifeCycle {
		/**
		 * Base name for all UI life cycle events
		 */
		public static final String TOPIC = UITopicBase + "/LifeCycle"; //$NON-NLS-1$

		/**
		 * Sent when a UIElement is brought to top
		 */
		public static final String BRINGTOTOP = "bringToTop"; //$NON-NLS-1$

		/**
		 * Sent when an MPart is activated
		 */
		public static final String ACTIVATE = "activate"; //$NON-NLS-1$
	}

	/**
	 * Method for constructing a topic string to subscribe to.
	 * 
	 * @param topic
	 * @return a topic that will match all model changes for the given topic.
	 */
	public static String buildTopic(String topic) {
		return topic + TOPIC_SEP + ALL_SUB_TOPICS;
	}

	/**
	 * Method for constructing a topic string to subscribe to.
	 * 
	 * @param topic
	 * @param attributeName
	 *            the UI model attribute to match
	 * @return a topic that will match a particular attribute for a particular model element
	 */
	public static String buildTopic(String topic, String attributeName) {
		return topic + TOPIC_SEP + attributeName + TOPIC_SEP + ALL_SUB_TOPICS;
	}

	/**
	 * Method for constructing a topic string to subscribe to.
	 * 
	 * @param topic
	 * @param attributeName
	 * @param eventType
	 * @return a topic that will match a particular eventType for a particular attribute of a
	 *         particular model element
	 */
	public static String buildTopic(String topic, String attributeName, String eventType) {
		return topic + TOPIC_SEP + attributeName + TOPIC_SEP + eventType;
	}

	/*************************************************************************************
	 * GENERATED CODE!!
	 * 
	 * NOTE: *All* non-generated code must be above this comment.
	 * 
	 * Replace the generated code below this comment with the output of GenTopic.
	 * 
	 *************************************************************************************/

	@SuppressWarnings("javadoc")
	public static interface BindingContext {
		public static final String TOPIC = UIModelTopicBase + "/commands/BindingContext"; //$NON-NLS-1$
		public static final String CHILDREN = "children"; //$NON-NLS-1$
		public static final String DESCRIPTION = "description"; //$NON-NLS-1$
		public static final String NAME = "name"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface BindingTable {
		public static final String TOPIC = UIModelTopicBase + "/commands/BindingTable"; //$NON-NLS-1$
		public static final String BINDINGCONTEXT = "bindingContext"; //$NON-NLS-1$
		public static final String BINDINGS = "bindings"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface BindingTableContainer {
		public static final String TOPIC = UIModelTopicBase + "/commands/BindingTableContainer"; //$NON-NLS-1$
		public static final String BINDINGTABLES = "bindingTables"; //$NON-NLS-1$
		public static final String ROOTCONTEXT = "rootContext"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface Bindings {
		public static final String TOPIC = UIModelTopicBase + "/commands/Bindings"; //$NON-NLS-1$
		public static final String BINDINGCONTEXTS = "bindingContexts"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface Category {
		public static final String TOPIC = UIModelTopicBase + "/commands/Category"; //$NON-NLS-1$
		public static final String DESCRIPTION = "description"; //$NON-NLS-1$
		public static final String NAME = "name"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface Command {
		public static final String TOPIC = UIModelTopicBase + "/commands/Command"; //$NON-NLS-1$
		public static final String CATEGORY = "category"; //$NON-NLS-1$
		public static final String COMMANDNAME = "commandName"; //$NON-NLS-1$
		public static final String DESCRIPTION = "description"; //$NON-NLS-1$
		public static final String PARAMETERS = "parameters"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface CommandParameter {
		public static final String TOPIC = UIModelTopicBase + "/commands/CommandParameter"; //$NON-NLS-1$
		public static final String NAME = "name"; //$NON-NLS-1$
		public static final String OPTIONAL = "optional"; //$NON-NLS-1$
		public static final String TYPEID = "typeId"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface Handler {
		public static final String TOPIC = UIModelTopicBase + "/commands/Handler"; //$NON-NLS-1$
		public static final String COMMAND = "command"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface HandlerContainer {
		public static final String TOPIC = UIModelTopicBase + "/commands/HandlerContainer"; //$NON-NLS-1$
		public static final String HANDLERS = "handlers"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface KeyBinding {
		public static final String TOPIC = UIModelTopicBase + "/commands/KeyBinding"; //$NON-NLS-1$
		public static final String COMMAND = "command"; //$NON-NLS-1$
		public static final String PARAMETERS = "parameters"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface KeySequence {
		public static final String TOPIC = UIModelTopicBase + "/commands/KeySequence"; //$NON-NLS-1$
		public static final String KEYSEQUENCE = "keySequence"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface Parameter {
		public static final String TOPIC = UIModelTopicBase + "/commands/Parameter"; //$NON-NLS-1$
		public static final String NAME = "name"; //$NON-NLS-1$
		public static final String VALUE = "value"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface PartDescriptor {
		public static final String TOPIC = UIModelTopicBase + "/basic/PartDescriptor"; //$NON-NLS-1$
		public static final String ALLOWMULTIPLE = "allowMultiple"; //$NON-NLS-1$
		public static final String CATEGORY = "category"; //$NON-NLS-1$
		public static final String CLOSEABLE = "closeable"; //$NON-NLS-1$
		public static final String CONTRIBUTIONURI = "contributionURI"; //$NON-NLS-1$
		public static final String DESCRIPTION = "description"; //$NON-NLS-1$
		public static final String DIRTYABLE = "dirtyable"; //$NON-NLS-1$
		public static final String MENUS = "menus"; //$NON-NLS-1$
		public static final String TOOLBAR = "toolbar"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface PartDescriptorContainer {
		public static final String TOPIC = UIModelTopicBase + "/basic/PartDescriptorContainer"; //$NON-NLS-1$
		public static final String DESCRIPTORS = "descriptors"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface Application {
		public static final String TOPIC = UIModelTopicBase + "/application/Application"; //$NON-NLS-1$
		public static final String ADDONS = "addons"; //$NON-NLS-1$
		public static final String CATEGORIES = "categories"; //$NON-NLS-1$
		public static final String COMMANDS = "commands"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface ApplicationElement {
		public static final String TOPIC = UIModelTopicBase + "/application/ApplicationElement"; //$NON-NLS-1$
		public static final String CONTRIBUTORURI = "contributorURI"; //$NON-NLS-1$
		public static final String ELEMENTID = "elementId"; //$NON-NLS-1$
		public static final String TAGS = "tags"; //$NON-NLS-1$
		public static final String TRANSIENTDATA = "transientData"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface Contribution {
		public static final String TOPIC = UIModelTopicBase + "/application/Contribution"; //$NON-NLS-1$
		public static final String CONTRIBUTIONURI = "contributionURI"; //$NON-NLS-1$
		public static final String OBJECT = "object"; //$NON-NLS-1$
		public static final String PERSISTEDSTATE = "persistedState"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface StringToObjectMap {
		public static final String TOPIC = UIModelTopicBase + "/application/StringToObjectMap"; //$NON-NLS-1$
		public static final String KEY = "key"; //$NON-NLS-1$
		public static final String VALUE = "value"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface StringToStringMap {
		public static final String TOPIC = UIModelTopicBase + "/application/StringToStringMap"; //$NON-NLS-1$
		public static final String KEY = "key"; //$NON-NLS-1$
		public static final String VALUE = "value"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface Perspective {
		public static final String TOPIC = UIModelTopicBase + "/advanced/Perspective"; //$NON-NLS-1$
		public static final String WINDOWS = "windows"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface Placeholder {
		public static final String TOPIC = UIModelTopicBase + "/advanced/Placeholder"; //$NON-NLS-1$
		public static final String CLOSEABLE = "closeable"; //$NON-NLS-1$
		public static final String REF = "ref"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface Part {
		public static final String TOPIC = UIModelTopicBase + "/basic/Part"; //$NON-NLS-1$
		public static final String CLOSEABLE = "closeable"; //$NON-NLS-1$
		public static final String DESCRIPTION = "description"; //$NON-NLS-1$
		public static final String MENUS = "menus"; //$NON-NLS-1$
		public static final String TOOLBAR = "toolbar"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface TrimmedWindow {
		public static final String TOPIC = UIModelTopicBase + "/basic/TrimmedWindow"; //$NON-NLS-1$
		public static final String TRIMBARS = "trimBars"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface Window {
		public static final String TOPIC = UIModelTopicBase + "/basic/Window"; //$NON-NLS-1$
		public static final String HEIGHT = "height"; //$NON-NLS-1$
		public static final String MAINMENU = "mainMenu"; //$NON-NLS-1$
		public static final String SHAREDELEMENTS = "sharedElements"; //$NON-NLS-1$
		public static final String WIDTH = "width"; //$NON-NLS-1$
		public static final String WINDOWS = "windows"; //$NON-NLS-1$
		public static final String X = "x"; //$NON-NLS-1$
		public static final String Y = "y"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface Context {
		public static final String TOPIC = UIModelTopicBase + "/ui/Context"; //$NON-NLS-1$
		public static final String CONTEXT = "context"; //$NON-NLS-1$
		public static final String PROPERTIES = "properties"; //$NON-NLS-1$
		public static final String VARIABLES = "variables"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface CoreExpression {
		public static final String TOPIC = UIModelTopicBase + "/ui/CoreExpression"; //$NON-NLS-1$
		public static final String COREEXPRESSION = "coreExpression"; //$NON-NLS-1$
		public static final String COREEXPRESSIONID = "coreExpressionId"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface Dirtyable {
		public static final String TOPIC = UIModelTopicBase + "/ui/Dirtyable"; //$NON-NLS-1$
		public static final String DIRTY = "dirty"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface ElementContainer {
		public static final String TOPIC = UIModelTopicBase + "/ui/ElementContainer"; //$NON-NLS-1$
		public static final String CHILDREN = "children"; //$NON-NLS-1$
		public static final String SELECTEDELEMENT = "selectedElement"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface GenericTile {
		public static final String TOPIC = UIModelTopicBase + "/ui/GenericTile"; //$NON-NLS-1$
		public static final String HORIZONTAL = "horizontal"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface GenericTrimContainer {
		public static final String TOPIC = UIModelTopicBase + "/ui/GenericTrimContainer"; //$NON-NLS-1$
		public static final String SIDE = "side"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface Input {
		public static final String TOPIC = UIModelTopicBase + "/ui/Input"; //$NON-NLS-1$
		public static final String INPUTURI = "inputURI"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface SnippetContainer {
		public static final String TOPIC = UIModelTopicBase + "/ui/SnippetContainer"; //$NON-NLS-1$
		public static final String SNIPPETS = "snippets"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface UIElement {
		public static final String TOPIC = UIModelTopicBase + "/ui/UIElement"; //$NON-NLS-1$
		public static final String ACCESSIBILITYPHRASE = "accessibilityPhrase"; //$NON-NLS-1$
		public static final String CONTAINERDATA = "containerData"; //$NON-NLS-1$
		public static final String CURSHAREDREF = "curSharedRef"; //$NON-NLS-1$
		public static final String ONTOP = "onTop"; //$NON-NLS-1$
		public static final String PARENT = "parent"; //$NON-NLS-1$
		public static final String RENDERER = "renderer"; //$NON-NLS-1$
		public static final String TOBERENDERED = "toBeRendered"; //$NON-NLS-1$
		public static final String VISIBLE = "visible"; //$NON-NLS-1$
		public static final String VISIBLEWHEN = "visibleWhen"; //$NON-NLS-1$
		public static final String WIDGET = "widget"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface UILabel {
		public static final String TOPIC = UIModelTopicBase + "/ui/UILabel"; //$NON-NLS-1$
		public static final String ICONURI = "iconURI"; //$NON-NLS-1$
		public static final String LABEL = "label"; //$NON-NLS-1$
		public static final String TOOLTIP = "tooltip"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface HandledItem {
		public static final String TOPIC = UIModelTopicBase + "/menu/HandledItem"; //$NON-NLS-1$
		public static final String COMMAND = "command"; //$NON-NLS-1$
		public static final String PARAMETERS = "parameters"; //$NON-NLS-1$
		public static final String WBCOMMAND = "wbCommand"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface Item {
		public static final String TOPIC = UIModelTopicBase + "/menu/Item"; //$NON-NLS-1$
		public static final String ENABLED = "enabled"; //$NON-NLS-1$
		public static final String SELECTED = "selected"; //$NON-NLS-1$
		public static final String TYPE = "type"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface Menu {
		public static final String TOPIC = UIModelTopicBase + "/menu/Menu"; //$NON-NLS-1$
		public static final String ENABLED = "enabled"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface MenuContribution {
		public static final String TOPIC = UIModelTopicBase + "/menu/MenuContribution"; //$NON-NLS-1$
		public static final String PARENTID = "parentId"; //$NON-NLS-1$
		public static final String POSITIONINPARENT = "positionInParent"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface MenuContributions {
		public static final String TOPIC = UIModelTopicBase + "/menu/MenuContributions"; //$NON-NLS-1$
		public static final String MENUCONTRIBUTIONS = "menuContributions"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface MenuElement {
		public static final String TOPIC = UIModelTopicBase + "/menu/MenuElement"; //$NON-NLS-1$
		public static final String MNEMONICS = "mnemonics"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface OpaqueMenuItem {
		public static final String TOPIC = UIModelTopicBase + "/menu/OpaqueMenuItem"; //$NON-NLS-1$
		public static final String OPAQUEITEM = "opaqueItem"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface OpaqueMenuSeparator {
		public static final String TOPIC = UIModelTopicBase + "/menu/OpaqueMenuSeparator"; //$NON-NLS-1$
		public static final String OPAQUEITEM = "opaqueItem"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface OpaqueToolItem {
		public static final String TOPIC = UIModelTopicBase + "/menu/OpaqueToolItem"; //$NON-NLS-1$
		public static final String OPAQUEITEM = "opaqueItem"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface RenderedMenu {
		public static final String TOPIC = UIModelTopicBase + "/menu/RenderedMenu"; //$NON-NLS-1$
		public static final String CONTRIBUTIONMANAGER = "contributionManager"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface RenderedMenuItem {
		public static final String TOPIC = UIModelTopicBase + "/menu/RenderedMenuItem"; //$NON-NLS-1$
		public static final String CONTRIBUTIONITEM = "contributionItem"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface RenderedToolBar {
		public static final String TOPIC = UIModelTopicBase + "/menu/RenderedToolBar"; //$NON-NLS-1$
		public static final String CONTRIBUTIONMANAGER = "contributionManager"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface ToolBarContribution {
		public static final String TOPIC = UIModelTopicBase + "/menu/ToolBarContribution"; //$NON-NLS-1$
		public static final String PARENTID = "parentId"; //$NON-NLS-1$
		public static final String POSITIONINPARENT = "positionInParent"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface ToolBarContributions {
		public static final String TOPIC = UIModelTopicBase + "/menu/ToolBarContributions"; //$NON-NLS-1$
		public static final String TOOLBARCONTRIBUTIONS = "toolBarContributions"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface ToolItem {
		public static final String TOPIC = UIModelTopicBase + "/menu/ToolItem"; //$NON-NLS-1$
		public static final String MENU = "menu"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface TrimContribution {
		public static final String TOPIC = UIModelTopicBase + "/menu/TrimContribution"; //$NON-NLS-1$
		public static final String PARENTID = "parentId"; //$NON-NLS-1$
		public static final String POSITIONINPARENT = "positionInParent"; //$NON-NLS-1$
	}

	@SuppressWarnings("javadoc")
	public static interface TrimContributions {
		public static final String TOPIC = UIModelTopicBase + "/menu/TrimContributions"; //$NON-NLS-1$
		public static final String TRIMCONTRIBUTIONS = "trimContributions"; //$NON-NLS-1$
	}
}