package com.jianghongkui.lib_rules

import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.jianghongkui.lib_rules.base.BaseIssue
import com.jianghongkui.lib_rules.base.Level.HIGH
import com.jianghongkui.lib_rules.base.Level.MIDDLE
import com.jianghongkui.lib_rules.detector.*

/**
 * 赋值相关规则
 *
 * @author jianghongkui
 * @date 2018/10/10
 */
object ValueIssue : BaseIssue() {
    @JvmField
    val ISSUE_FONT = createXmlIssue(
            "FontUsage",
            "禁止直接设置字体大小",
            "请使用font_size.xml中的字体值",
            HIGH,
            XmlValueDetector::class.java)

    @JvmField
    val ISSUE_COLOR = createXmlIssue(
            "ColorUsage",
            "禁止直接设置颜色",
            "请使用colors.xml中的颜色",
            HIGH,
            XmlValueDetector::class.java)

    fun getAllIssue(): List<Issue> = listOf(ISSUE_FONT, ISSUE_COLOR)

}

/**
 * 问题分析相关规则
 *
 * @author jianghongkui
 * @date 2018/10/10
 */
object AnalysisIssue : BaseIssue() {
    @JvmField
    val ISSUE_CLASS_LINES: Issue = createJavaIssue(
            "ClassLines",
            "类尽量保持在800行以内",
            "类的职责需要保持单一性，避免负责过多功能导致臃肿，影响后续维护",
            HIGH,
            ClassDetector::class.java)

    @JvmField
    val ISSUE_METHOD_LINES: Issue = createJavaIssue(
            "MethodLines",
            "一个方法尽量保持在50行以内",
            "如果方法超过50行，请抽离出其他方法，以保持功能单一",
            MIDDLE,
            MethodDetector::class.java)

    @JvmField
    val ISSUE_SHAPE_LINE: Issue = createXmlIssue(
            "ShpeLine",
            "虚线的高度(android:width)应该大于布局高度（Layout_height）",
            "使用xml文件作虚线时，虚线所在的布局高度（Layout_height）必须大于虚线本身的高度（stroke中的width）" +
                    "(等于也不行)，否则，在4.4以上版本，虚线不会显示出来",
            HIGH,
            DrawableDetector::class.java)

    fun getAllIssue(): List<Issue> = listOf(ISSUE_CLASS_LINES, ISSUE_METHOD_LINES, ISSUE_SHAPE_LINE)

}

/**
 * SDK使用相关规则
 *
 * @author jianghongkui
 * @date 2018/10/10
 */
object UsageIssue : BaseIssue() {

    @JvmField
    val ISSUE_THREAD: Issue = createJavaIssue(
            "ThreadUsage",
            "避免自己创建线程",
            "请勿直接调用new Thread()，请统一使用ExThead",
            HIGH,
            ThreadUsageDetector::class.java)

    @JvmField
    val ISSUE_HANDLER: Issue = createJavaIssue(
            "HandlerUsage",
            "禁止创建handler",
            "Handler使用不当会导致内存泄露，尽量不要使用",
            HIGH,
            ThreadUsageDetector::class.java)

    @JvmField
    val ISSUE_LOG: Issue = createJavaIssue(
            "LogUsage",
            "避免使用android.util.Log",
            "请勿直接调用android.util.Log，应该使用统一工具类ExLog",
            HIGH,
            MethodCallUsageDetector::class.java)

    @JvmField
    val ISSUE_EQUALS: Issue = createJavaIssue(
            "EqualUsage",
            "判断等值是把常量放在前面",
            "判断等值是把常量放在前面",
            MIDDLE,
            MethodCallUsageDetector::class.java)

    @JvmField
    val ISSUE_DEFAULT: Issue = createJavaIssue(
            "SwitchDefaultUsage",
            "Switch语句必须包含default",
            "在一个 switch 块内,都必须包含一个 default 语句并且 放在最后,即使它什么代码也没有。",
            MIDDLE,
            SwitchUsageDetector::class.java)

    @JvmField
    val ISSUE_BREAK: Issue = createJavaIssue(
            "SwitchBreakUsage",
            "Switch语句必须包含break或return",
            "在一个 switch 块内,每个 case 要么通过 break/return 等来终止,要么注释说明程序将继续执行" + "到哪一个case为止;",
            MIDDLE,
            SwitchUsageDetector::class.java)

    @JvmField
    val ISSUE_SIMPLE_DATE_FORMAT: Issue = createJavaIssue(
            "SimpleDateFormatUsage",
            "SimpleDateFormatUsage不能定义为static",
            "SimpleDateFormat 是线程不安全的类,一般不要定义为static变量,如果定义为static,必须加锁," + "或者使用 DateUtils工具类。",
            HIGH,
            SimpleDateFormatUsageDetector::class.java)

    @JvmField
    val ISSUE_IF: Issue = createJavaIssue(
            "IfUsage",
            "表达异常的分支时,少用 if-else 方式",
            "表达异常的分支时,少用 if-else 方式,这种方式可 以改写成: \u2028 if (condition) { ... \u2028 return obj; } \u2028// " +
                    "接着写 else 的业\n务逻辑代码;\u2028",
            MIDDLE,
            IfUsageDetector::class.java)

    @JvmField
    val ISSUE_FOREACH: Issue = createJavaIssue(
            "ForeachUsage",
            "禁止在循环体重操作集合",
            "不要在 foreach 循环里进行元素的 remove/add 操作。remove 元素请使用 Iterator方式 ",
            HIGH,
            LoopUsageDetector::class.java)

    @JvmField
    val ISSUE_LOOP: Issue = createJavaIssue(
            "LoopUsage",
            "尽量不用再循环体声明变量和try",
            "循环体中的语句要考量性能,以下操作尽量移至 循环体外处理,如定义对象、变量、 \u2028获取数据库连接," +
                    "进行 不必要的 try-catch 操作(这个 try-catch 是否可以移至循环 体外)。 \u2028",
            MIDDLE,
            LoopUsageDetector::class.java)

    @JvmField
    val ISSUE_COLLECTION_INIT: Issue = createJavaIssue(
            "CollectionInitUsage",
            "集合初始化时,指定集合初始值大小",
            "集合初始化时,指定集合初始值大小",
            MIDDLE,
            CollectionDetector::class.java)

    @JvmField
    val ISSUE_SET: Issue = createJavaIssue(
            "SetUsage",
            "使用 entrySet 遍历 Map 类集合 KV,而不是 keySet 方式进行遍历",
            "keySet 其实是遍历了 2 次,一次是转为 Iterator 对象, 另一次是从 hashMap 中取出key所对" +
                    "应的value。而entrySet只是遍历了一次就把 key 和 value 都放到了 entry 中,效 率更高。如果是 " +
                    "JDK8,使用 Map.foreach 方法。",
            MIDDLE,
            CollectionDetector::class.java)


    //    public static final Issue ISSUE_MAP:Issue = createJavaIssue(
    //            "MapUsage",
    //            "高度注意 Map 类集合 K/V 能不能存储 null 值的情况",
    //            "",
    //            MIDDLE,
    //            CollectionDetector.class);
    @JvmField
    val ISSUE_LAYOUT: Issue = createXmlIssue(
            "LayoutUsage",
            "layout层数不能大于三层",
            "layout层数过多会导致绘制时间过长，造成卡顿，因此不建议超过三层",
            MIDDLE,
            LayoutLevelDetector::class.java)

    @JvmField
    val ISSUE_SHAPE_BACKGROUND: Issue = createXmlIssue(
            "ShapeBackgroundUsage",
            "使用shape做背景需要添加solid",
            "使用shape做背景如不添加solid，在三星老手机上显示成了黑色，而其他手机显示为透明",
            HIGH,
            DrawableDetector::class.java)

//    @JvmField
//    val ISSUE_TEXTVIEW: Issue = createXmlIssue(
//            "TextViewUsage",
//            "",
//            "",
//            MIDDLE,
//            TextViewUsageDetector::class.java)

//    @JvmField
//    val ISSUE_EDITTEXT: Issue = createJavaIssue(
//            "EditTextUsage",
//            "",
//            "",
//            MIDDLE,
//            EditTextDetector::class.java)

    @JvmField
    val ISSUE_SCROLLVIEW: Issue = createJavaIssue(
            "ScrollViewUsage",
            "使用scrollView的onOverScrolled需注意clampedY的值",
            "在锤子手机上clampedY可能永远为false",
            MIDDLE,
            ScrollViewUsageDetector::class.java)

    @JvmField
    val ISSUE_SET_ALPHA: Issue = createJavaIssue(
            "setAlphaUsage",
            "直接使用setAlpha可能会导致其他视图颜色改变，请先调用mutate函数",
            "",
            HIGH,
            MethodCallUsageDetector::class.java)

    @JvmField
    val ISSUE_WHITE_BACKGROUND: Issue = createXmlIssue(
            "WhiteBackgroundUsage",
            "背景设置为纯白色在部分机型上为透明",
            "",
            HIGH,
            WhiteColorUsageDetector::class.java)

    val ISSUE_ENUM: Issue = createXmlIssue(
            "EnumUsage",
            "禁止使用枚举类型",
            "",
            HIGH,
            ClassDetector::class.java)

    fun getAllIssue(): List<Issue> = listOf(ISSUE_THREAD, ISSUE_HANDLER/*, ISSUE_LOG*/, ISSUE_EQUALS, ISSUE_DEFAULT,
            ISSUE_BREAK, ISSUE_SIMPLE_DATE_FORMAT, ISSUE_IF, ISSUE_FOREACH, ISSUE_LOOP, ISSUE_COLLECTION_INIT,
            ISSUE_SET, ISSUE_LAYOUT, ISSUE_SHAPE_BACKGROUND/*, ISSUE_TEXTVIEW, ISSUE_EDITTEXT*/, ISSUE_SCROLLVIEW,
            ISSUE_SET_ALPHA, ISSUE_WHITE_BACKGROUND,ISSUE_ENUM)
}

/**
 * 中文使用规则
 *
 * @author jianghongkui
 * @date 2018/10/10
 */
object NoChineseIssue : BaseIssue() {
    @JvmField
    val ISSUE_JAVA: Issue = createJavaIssue(
            "NoChineseForJava",
            "请勿使用中文",
            "禁止在java代码中直接设置中文变量。如需使用中文，请在strings.xml中创建", HIGH,
            ClassDetector::class.java)

    @JvmField
    val ISSUE_XML: Issue = createXmlIssue(
            "NoChineseForXML",
            "请勿使用中文",
            "禁止在布局文件中直接设置中文变量。如需使用中文，请在strings.xml中创建", HIGH,
            XmlValueDetector::class.java)

    fun getAllIssue(): List<Issue> = listOf(ISSUE_JAVA, ISSUE_XML)

}

/**
 * 命名规则
 *
 * @author jianghongkui
 * @date 2018/10/10
 */
object NameIssue : BaseIssue() {

    private const val LOWER_CAMEL_CASE: String = "驼峰命名规则：第一个单词小写，其他单词首字母大写；仅能包含字母"
    @JvmField
    val ISSUE_ID: Issue = createXmlIssue(
            "IdName",
            "id统一使用小写字母和下划线",
            "", HIGH,
            XmlValueDetector::class.java)

    @JvmField
    val ISSUE_NAME: Issue = createXmlIssue(
            "ValueName",
            "name统一使用小写字母和下划线",
            "", HIGH,
            ValueDetector::class.java)

    @JvmField
    val ISSUE_CLASS: Issue = createJavaIssue(
            "ClassName",
            "类名命名错误",
            "类命名只用使用大小写字母，", HIGH,
            ClassDetector::class.java)

    @JvmField
    val ISSUE_VAR: Issue = createJavaIssue(
            "VariableName",
            "变量应以驼峰格式命名",
            LOWER_CAMEL_CASE, HIGH,
            VariableDetector::class.java)

    @JvmField
    val ISSUE_ARRAY: Issue = createJavaIssue(
            "ArrayName",
            "数组声明时中括号放在类型后面",
            LOWER_CAMEL_CASE, HIGH,
            VariableDetector::class.java)

    @JvmField
    val ISSUE_VAL: Issue = createJavaIssue(
            "ConstantName",
            "静态常量只能使用大写字母和下划线",
            LOWER_CAMEL_CASE, HIGH,
            VariableDetector::class.java)

    @JvmField
    val ISSUE_METHOD: Issue = createJavaIssue(
            "MethodName",
            "函数名应以驼峰格式命名",
            LOWER_CAMEL_CASE, HIGH,
            MethodDetector::class.java)

    @JvmField
    val ISSUE_PARAMS: Issue = createJavaIssue(
            "VariableName",
            "变量应以驼峰格式命名",
            LOWER_CAMEL_CASE, HIGH,
            VariableDetector::class.java)

    fun getAllIssue(): List<Issue> = listOf(ISSUE_ID, ISSUE_NAME, ISSUE_CLASS, ISSUE_VAR, ISSUE_ARRAY, ISSUE_VAL,
            ISSUE_METHOD, ISSUE_PARAMS)

}

/**
 * 代码格式规则
 *
 * @author jianghongkui
 * @date 2018/10/10
 */
object FormatIssue : BaseIssue() {
    @JvmField
    val ISSUE_LAYOUT: Issue = createXmlIssue(
            "LayoutFormat",
            "Layout格式有误",
            "使用快捷键格式代码(windows:Ctrl+shift+L mac:command+option+L)",
            MIDDLE,
            LayoutLevelDetector::class.java)

    @JvmField
    val ISSUE_RESERVED_WORD: Issue = createJavaIssue(
            "ReservedWord",
            "if/for/while/switch/do 等保留字与括号之间都必须加空格",
            "if/for/while/switch/do 等保留字与括号之间都必须加空格",
            HIGH,
            ReservedWordDetector::class.java)

    @JvmField
    val ISSUE_CONSTRUCTOR_ORDER_RULE: Issue = createJavaIssue(
            "ConstructorOrderRule",
            "构造方法应该用递增的方式写（参数少的写在前面）",
            "构造方法应该用递增的方式写（参数少的写在前面）",
            MIDDLE,
            ConstructorDetector::class.java)

    @JvmField
    val ISSUE_CONSTRUCTOR_RULE: Issue = createJavaIssue(
            "ConstructorRule",
            "构造方法应该保证参数少的调用参数多的构造方法",
            "构造方法应该保证参数少的调用参数多的构造方法",
            MIDDLE,
            ConstructorDetector::class.java)



    @JvmField
    val ISSUE_CONSTRUCTOR_SCHEDULE_RULE: Issue = createJavaIssue(
            "ConstructorScheduleRule",
            "构造方法应该保证在所有方法之前且成递增样式排列",
            "构造方法应该保证在所有方法之前且成递增样式排列",
            HIGH,
            ConstructorDetector::class.java)


    @JvmField
    val ISSUE_OPERATOR_RULE: Issue = createJavaIssue(
            "OperatorFormatRule",
            "任何二目运算符的左右两边都需要加一个空格。",
            "任何二目运算符的左右两边都需要加一个空格。",
            HIGH,
            OperatorFormatDetector::class.java)

    @JvmField
    val ISSUE_UNARY_OPERATOR_RULE: Issue = createJavaIssue(
            "UnaryOperatorFormatRule",
            "任何单目运算符的左右两边都不需要加一个空格。",
            "",
            HIGH,
            OperatorFormatDetector::class.java)

    @JvmField
    val ISSUE_TERNARY_OPERATOR_RULE: Issue = createJavaIssue(
            "TernaryOperatorFormatRule",
            "三目运算符的左右两边都需要加一个空格",
            "",
            HIGH,
            OperatorFormatDetector::class.java)


    @JvmField
    val ISSUE_LINE_RULE: Issue = createJavaIssue(
            "LineFormatRule",
            "方法与类成员间行距一行，不要紧贴着。",
            "方法与类成员间行距一行，不要紧贴着。",
            HIGH,
            IntervalSpaceDetector::class.java)

    fun getAllIssue(): List<Issue> = listOf(ISSUE_LAYOUT, ISSUE_RESERVED_WORD,
            ISSUE_CONSTRUCTOR_ORDER_RULE, ISSUE_CONSTRUCTOR_RULE, ISSUE_OPERATOR_RULE, ISSUE_LINE_RULE,
            ISSUE_UNARY_OPERATOR_RULE, ISSUE_TERNARY_OPERATOR_RULE)

}

/**
 * 注释规则
 *
 * @author jianghongkui
 * @date 2018/10/10
 */
object CommentIssue : BaseIssue() {
    @JvmField
    val ISSUE_CLASS: Issue = createJavaIssue(
            "ClassComment",
            "公共类都必须添加注释",
            "类注释必须使用Javadoc规范,使用 /**内容*/格式,不得使用// xxx方式，并且需要添加创建者和创建时间",
            HIGH,
            ClassDetector::class.java)

    @JvmField
    val ISSUE_METHOD: Issue = createJavaIssue(
            "MethodComment",
            "方法注释必须使用 /**内容*/格式",
            "方法注释必须使用Javadoc规范,使用 /**内容*/格式,不得使用// xxx方式",
            MIDDLE,
            MethodDetector::class.java)

    @JvmField
    val ISSUE_VAL: Issue = createJavaIssue(
            "ValComment",
            "成员变量必须使用单行注释 ",
            "", MIDDLE,
            VariableDetector::class.java)

    @JvmField
    val ISSUE_MANIFEST: Issue = createIssue(
            "ActivityManifestComment",
            "AndroidManifest.xml对Activity进行相应的注释，注明这个Activity的业务页面",
            "",
            HIGH,
            Implementation(ManifestCommentDetector::class.java, Scope.MANIFEST_SCOPE))

    fun getAllIssue(): List<Issue> = listOf(ISSUE_CLASS, ISSUE_METHOD, ISSUE_VAL, ISSUE_MANIFEST)

}
