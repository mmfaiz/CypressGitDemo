<%--
  Created by IntelliJ IDEA.
  User: victorlindhe
  Date: 2018-10-12
  Time: 13:19
--%>

<%@ page import="grails.converters.JSON" contentType="text/html;charset=UTF-8"%>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title>Admin encoding examples</title>
</head>

<body>

<div class="container">
    <div class="row">
        <div class="col-xs-12">
            <h1>Encoding examples</h1>
            <p>The purpose of this page is to show different encoding scenarios and how to prevent XSS by using the right methods. Buttons are provided to test JavaScript, where in good cases the variable should show as an alert but not in the danger examples.</p>
        </div>
    </div>
    <div class="row">
        <div class="col-xs-12">
            <h2>Safe cases</h2>
            <p>In these situations, it seems to be safe just to use the code as normal.</p>
        </div>
        <div class="col-xs-12">
            <h3>Just data in plain HTML</h3>
            <p>In plain HTML ("just on the page"), data is encoded by using the grails expression <code>${"\${}"}</code></p>
            <div class="well">
                <p><strong>Danger data:</strong><code> ${"<script>alert('Dangerous script');</script>"} </code></p>
                <p><strong>Code:</strong><code>${"\${dangerData}"}</code></p>
                <p><strong>Output:</strong> ${dangerData}</p>
            </div>
            <div class="well">
                <p><strong>Good data:</strong><code> "Name of a user"</code></p>
                <p><strong>Code:</strong><code>${"\${goodData}"}</code></p>
                <p><strong>Output:</strong> ${goodData}</p>
            </div>
        </div>
        <div class="col-xs-12">
            <h3>Examples with data attribute, since encoded no problems</h3>
            <p>Data attributes seems to be safe right now? Only tested in Google Chrome.</p>
            <div class="well">
                <p><strong>Danger data:</strong><code> " onclick="javascript:alert("WOHO");" data-tennis="</code></p>
                <p><strong>Code:</strong><code> ${"<strong data-name=\"\${dangerTagData2}\">Has dangerous data attribute</strong>"}</code></p>
                <p><strong>Output (see inspector):</strong> <strong data-name="${dangerTagData2}">Has dangerous data attribute</strong></p>
            </div>
            <div class="well">
                <p><strong>Danger data:</strong><code> "javascript:alert('Danger');"</code></p>
                <p><strong>Code:</strong><code> ${"<strong data-name=\"\${dangerTagData}\">Has dangerous data attribute</strong>"}</code></p>
                <p><strong>Output (see inspector):</strong> <strong data-name="${dangerTagData}">Has dangerous data attribute</strong></p>
            </div>
        </div>
        <div class="col-xs-12">
            <h2>Grails tags</h2>
            <p>Inconsistent according to <a href="https://www.slideshare.net/theratpack/xss-countermeasures-in-grails">this page of slides</a> which seems to be around the release of Grails 2.3, but here are some examples of common grails tags.</p>
        </div>
        <div class="col-xs-12">
            <h3>Message tags escapes parameters</h3>
            <div class="well">
                <p><strong>Danger data:</strong><code> ${"<script>alert('Dangerous script');</script>"} </code></p>
                <p><strong>Code:</strong><code> ${"<g:message code=\"default.add.label\" args=\"\${[dangerData]}\" />"}</code></p>
                <p><strong>Output:</strong> <g:message code="default.add.label" args="${[dangerData]}" /></p>
            </div>
            <div class="well">
                <p><strong>Danger data:</strong><code> ${"<script>alert('Dangerous script');</script>"} </code></p>
                <p><strong>Code:</strong><code> ${"\${message(code: \"default.add.label\", args: [dangerData])}"}</code></p>
                <p><strong>Output:</strong> ${message(code: "default.add.label", args: [dangerData])}</p>
            </div>
        </div>
        <div class="col-xs-12">
            <h3>CreateLink tags encodes url parameters correctly</h3>
            <div class="well">
                <p><strong>Danger data:</strong><code> ${"<script>alert('Dangerous script');</script>"} </code></p>
                <p><strong>Code:</strong><code> ${"<g:createLink params=\"\${[val: dangerData]}\" />"}</code></p>
                <p><strong>Output:</strong> <a href="<g:createLink params="${[val: dangerData]}" />">Hover this link</a></p>
            </div>
            <div class="well">
                <p><strong>Danger data:</strong><code> ${"<script>alert('Dangerous script');</script>"} </code></p>
                <p><strong>Code:</strong><code> ${"\${createLink(params: [val: dangerData])}"}</code></p>
                <p><strong>Output:</strong> <a href="${createLink(params: [val: dangerData])}">Hover this link</a></p>
            </div>
        </div>
        <div class="col-xs-12">
            <h3>g:textArea seems safe</h3>
            <div class="well">
                <p><strong>Danger data:</strong><code> ${"<script>alert('Dangerous script');</script>"} </code></p>
                <p><strong>Code:</strong><code> ${"<g:textArea name=\"dangerTextArea\" value=\"\${raw(dangerData)}\" />"}</code></p>
                <p><strong>Output:</strong> <g:textArea name="dangerTextArea" value="${raw(dangerData)}" /></p>
            </div>
            <h3>g:textField as well</h3>
            <div class="well">
                <p><strong>Danger data:</strong><code> ${"<script>alert('Dangerous script');</script>"} </code></p>
                <p><strong>Code:</strong><code> ${"<g:textField name=\"dangerTextField\" value=\"\${raw(dangerData)}\" />"}</code></p>
                <p><strong>Output:</strong> <g:textField name="dangerTextField" value="${raw(dangerData)}" /></p>
                <p><strong>Code:</strong><code> ${"<g:textField name=\"dangerTextField\" value=\"\${'\" onclick=\"alert();\"'}\" />"}</code></p>
                <p><strong>Output:</strong> <g:textField name="dangerTextField" value="${'" onclick="alert();"'}" /></p>
            </div>
        </div>

    </div>
    <div class="row">
        <div class="col-xs-12">
            <h2>Challenges and solutions</h2>

        </div>
        <div class="col-xs-12">
            <h3>Rich HTML</h3>
            <p>Rich HTML might contain dangerous code. To prevent this while allowing the html to be displayed, we can use <code>raw()</code> and <code>.encodeAsSanitizedMarkup()</code>.</p>
            <div class="well">
                <p><strong>Data contains:</strong><code> ${"\"<strong>This is ssome änna dåva HTML åså<script>alert('Danger!');</script></strong>\""} </code></p>
                <p><strong>Code:</strong><code> ${"\${raw(dangerHTML.encodeAsSanitizedMarkup())}"}</code></p>
                <p><strong>Output:</strong> ${raw(dangerHTML.encodeAsSanitizedMarkup())}</p>
            </div>
            <p>To make it easy we have implemented a tag, <code>.toRichHTML(text:)</code> which does this for us. </p>
            <div class="well">
                <p>How we do it at <span class="label label-success">MATCHi</span></p>
                <p><strong>MATCHi code:</strong><code> ${"\${g.toRichHTML(text: dangerHTML)}"} </code></p>
                <p><strong>MATCHi output:</strong> ${g.toRichHTML(text: dangerHTML)}</p>
            </div>
        </div>
        <div class="col-xs-12">
            <h3>In tag attributes</h3>
            <p>In tag attributes, <code>${"\${}"}</code> does not provide any safety.</p>
            <div class="well">
                <p><strong>Danger data:</strong><code> "javascript:alert('WOHO');"</code></p>
                <p><strong>Code:</strong><code> ${"<a href=\"\${dangerTagData}\">Link</a>"}</code></p>
                <p><strong>Output:</strong> <a href="${dangerTagData}">Link</a></p>
            </div>
            <p>In link with tel: prefix.</p>
            <div class="well">
                <p><strong>Danger data:</strong><code> "javascript:alert('WOHO');"</code></p>
                <p><strong>Code:</strong><code> ${"<a href=tel:\"\${dangerTagData}\">Link</a>"}</code></p>
                <p><strong>Output:</strong> <a href="tel:${dangerTagData}">Link</a></p>
            </div>
            <p>Example with handler, dangerous since the encoded stuff is executed</p>
            <div class="well">
                <p><strong>Danger data:</strong><code> "javascript:alert('Danger');"</code></p>
                <p><strong>Code:</strong><code> ${"<strong onclick=\"\${dangerTagData}\">Has dangerous event handler</strong>"}</code></p>
                <p><strong>Output:</strong> <strong onclick="${dangerTagData}">Has dangerous event handler, click to see</strong></p>
            </div>
            <div class="well">
                <p>How we do it at <span class="label label-success">MATCHi</span></p>
                <p><strong>Danger data:</strong><code> "javascript:alert('WOHO');"</code></p>
                <p><strong>MATCHi code:</strong><code> ${"<a href=\"http://localhost:8080/?hej=\${dangerTagData.encodeAsURL()}\">Link</a>"} </code></p>
                <p><strong>MATCHi output:</strong> <a href="http://localhost:8080/?hej=${dangerTagData.encodeAsURL()}">Link</a></p>
                <strong>Good data:</strong><code> "stuff that is supplied as variable"</code></p>
                <p><strong>MATCHi code:</strong><code> ${"<a href=\"http://localhost:8080/?hej=\${\"stuff that is supplied as variable\".encodeAsURL()}\">Link</a>"}</code></p>
                <p><strong>MATCHi output:</strong> <a href="http://localhost:8080/?hej=${"stuff that is supplied as variable".encodeAsURL()}">Link</a></p>
            </div>
        </div>

        <div class="col-xs-12">
            <h3>JSON objects</h3>
            <p>
                When using JSON in tag attributes, <code>${"\${}"}</code> does not provide any protection just like when using data in tag attributes. However, since the JSON converter protects we can convert the object directly in the view. If it cannot be converted, the page breaks and therefore protects against malicious code.
                This works even if the object is already a JSON object. Cannot provide bad example below since page would break. We have made this easy by implementing <code>.expectJsonInTag(json:)</code>. If the object supplied cannot be converted to JSON, we return an empty string.
            </p>
            <div class="well">
                <p>How we do it at <span class="label label-success">MATCHi</span></p>
                <strong>Good data:</strong><code> ['edca8b9a375ba54a01375baccc4b0000', 'edca8b9a375ba54a01375baccc4f0001', 'edca8b9a375ba54a01375baccc530002']</code></p>
                <p><strong>Code:</strong><code>${"<button id=\"jsonInTagGoodExampleButton\" class=\"btn btn-success\" data-slotids=\"\${g.expectJsonInTag(json: slotIds)}\"\">"}</code></p>
                <p><button id="jsonInTagGoodExampleButton2" class="btn btn-success" data-slotids="${g.expectJsonInTag(json: slotIds)}">Test (should work)</button></p>
                <p><strong>Output (see inspector):</strong>
                    <script>
                        $(document).ready(function() {
                            $('#jsonInTagGoodExampleButton2').bind('click', function () {
                                var slotIds = $(this).data('slotids')
                                alert("The first slotId is " + slotIds[0]);
                            });
                        });
                    </script>
                </p>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-xs-12">
            <h2>In JavaScript tags</h2>
            <p>When using pure data in JavaScript, <code>${"\${}"}</code> offers some protection but not enough. Use encodeAsJavaScript();</p>
            <div class="well">
                <p><strong>Danger data:</strong><code> "0; alert();"</code></p>
                <p><strong>Code:</strong><code>${"<script>var myVariable = \${raw(dangerJSData.encodeAsJavaScript())};</script>"}</code></p>
                <p><button id="dataInScriptDangerExampleButton" class="btn btn-danger">Test (should not work)</button></p>
                <p><strong>Output (see inspector):</strong>
                    <script>
                        $(document).ready(function() {
                            var myVariable = ${raw(dangerJSData.encodeAsJavaScript())};
                            $('#dataInScriptDangerExampleButton').bind('click', function () {
                                alert(myVariable);
                            });
                        });
                    </script>
                </p>
            </div>
            <div class="well">
                <p><strong>Good data:</strong><code> "Sune User Andersson ÅÄÖ123%&/"</code></p>
                <p><strong>Code:</strong><code>${"<script>var myVariable = \${raw(goodJSData.encodeAsJavaScript())};</script>"}</code></p>
                <p><button id="dataInScriptGoodExampleButton" class="btn btn-success">Test (should work)</button></p>
                <p><strong>Output (see inspector):</strong>
                    <script>
                        $(document).ready(function() {
                            var myVariable = "${raw(goodJSData.encodeAsJavaScript())}";
                            $('#dataInScriptGoodExampleButton').bind('click', function () {
                                alert(myVariable);
                            });
                        });
                    </script>
                </p>
            </div>
            <p>Using JSON in a script tag, we can use <code>raw()</code> and <code>.decodeHTML().</code></p>
            <div class="well">
                <p><strong>Danger data:</strong><code> ['"]alert("Danger");")"', 'Victor', 'Daniel']</code></p>
                <p><strong>Code:</strong><code> ${"var myVariable = \${raw(badJSON.decodeHTML())};"}</code></p>
                <p><button id="jsonInScriptDangerExampleButton" class="btn btn-danger">Test (should work showing the ugly data)</button></p>
                <p><strong>Output (see inspector):</strong>
                    <script>
                        $(document).ready(function() {
                            var myVariable = ${raw(badJSON.decodeHTML())};
                            $('#jsonInScriptDangerExampleButton').bind('click', function () {
                                alert(myVariable);
                            });
                        });
                    </script>
                </p>
            </div>
            <div class="well">
                <p><strong>Good data:</strong><code> ${goodJSON}</code></p>
                <p><strong>Code:</strong><code> ${"var myVariable = \${raw(goodJSON.decodeHTML())};"}</code></p>
                <p><button id="jsonInScriptGoodExampleButton" class="btn btn-success">Test (should work)</button></p>
                <p><strong>Output (see inspector):</strong>
                    <script>
                        $(document).ready(function() {
                            var myVariable = ${raw((goodJSON as grails.converters.JSON))};
                            $('#jsonInScriptGoodExampleButton').bind('click', function () {
                                alert(myVariable + " with the first element being " + myVariable[0]);
                            });
                        });
                    </script>
                </p>
            </div>
            <p>For both ordinary data and JSON objects, we have implemented the tag <code>.forJavaScript(json:, data:)</code>. If you supply both, the method will go with the json object.
            This tag uses <code>.expectJsonInTag(json:)</code> internally, providing the same protection for converting JSON objects.</p>
            <div class="well">
                <p>How we do it at <span class="label label-success">MATCHi</span></p>
                <p><strong>Danger data:</strong><code> "0; alert();"</code></p>
                <p><strong>Code:</strong><code>${"<script>var myVariable = \${g.forJavaScript(data: dangerJSData)};</script>"}</code></p>
                <p><button id="dataInScriptDangerExampleButton2" class="btn btn-danger">Test (should not work)</button></p>
                <p><strong>Output (see inspector):</strong>
                    <script>
                        $(document).ready(function() {
                            var myVariable = ${g.forJavaScript(data: dangerJSData)};
                            $('#dataInScriptDangerExampleButton2').bind('click', function () {
                                alert(myVariable);
                            });
                        });
                    </script>
                </p>
            </div>
            <div class="well">
                <p>How we do it at <span class="label label-success">MATCHi</span></p>
                <p><strong>Good data:</strong><code> "Sune User Andersson ÅÄÖ123%&/"</code></p>
                <p><strong>Code:</strong><code>${"<script>var myVariable = \${g.forJavaScript(data: goodJSData)};</script>"}</code></p>
                <p><button id="dataInScriptGoodExampleButton2" class="btn btn-success">Test (should work)</button></p>
                <p><strong>Output (see inspector):</strong>
                    <script>
                        $(document).ready(function() {
                            var myVariable = "${g.forJavaScript(data: goodJSData)}";
                            $('#dataInScriptGoodExampleButton2').bind('click', function () {
                                alert(myVariable);
                            });
                        });
                    </script>
                </p>
            </div>
            <div class="well">
                <p>How we do it at <span class="label label-success">MATCHi</span></p>
                <p><strong>Danger data:</strong><code> ['"]alert("Danger");")"', 'Victor', 'Daniel']</code></p>
                <p><strong>Code:</strong><code> ${"var myVariable = \${g.forJavaScript(json: badJSON)};"}</code></p>
                <p><button id="jsonInScriptDangerExampleButton2" class="btn btn-danger">Test (should work showing the ugly data)</button></p>
                <p><strong>Output (see inspector):</strong>
                    <script>
                        $(document).ready(function() {
                            var myVariable = ${g.forJavaScript(json: badJSON)};
                            $('#jsonInScriptDangerExampleButton2').bind('click', function () {
                                alert(myVariable);
                            });
                        });
                    </script>
                </p>
            </div>
            <div class="well">
                <p>How we do it at <span class="label label-success">MATCHi</span></p>
                <p><strong>Good data:</strong><code> ${goodJSON}</code></p>
                <p><strong>Code:</strong><code> ${"var myVariable = \${g.forJavaScript(json: goodJSON)};"}</code></p>
                <p><button id="jsonInScriptGoodExampleButton2" class="btn btn-success">Test (should work)</button></p>
                <p><strong>Output (see inspector):</strong>
                    <script>
                        $(document).ready(function() {
                            var myVariable = ${g.forJavaScript(json: goodJSON)};
                            $('#jsonInScriptGoodExampleButton2').bind('click', function () {
                                alert(myVariable + " with the first element being " + myVariable[0]);
                            });
                        });
                    </script>
                </p>
            </div>
            <div class="well">
                <p>How we do it at <span class="label label-success">MATCHi</span></p>
                <p><strong>Good url:</strong><code> ${goodTagData}</code></p>
                <p><strong>Code:</strong><code> ${"var myVariable = \"\${g.forJavaScript(data: goodTagData)};\""}</code></p>
                <p><button id="jsonInScriptGoodExampleButton3" class="btn btn-success">Test (should work with redirection)</button></p>
                <p><strong>Output (see inspector):</strong>
                    <script>
                        $(document).ready(function() {
                            var myVariable = "${g.forJavaScript(data: goodTagData)}";
                            $('#jsonInScriptGoodExampleButton3').bind('click', function () {
                                location.href = myVariable;
                            });
                        });
                    </script>
                </p>
            </div>
            <div class="well">
                <p>How we do it at <span class="label label-success">MATCHi</span></p>
                <p><strong>Bad url:</strong><code> ${cookieStealingUrl}</code></p>
                <p><strong>Code:</strong><code> ${"var myVariable = \"\${g.forJavaScript(data: cookieStealingUrl)};\""}</code></p>
                <p><button id="jsonInScriptBadExampleButton3" class="btn b1.tn-success">Test (should redirect everything encoded)</button></p>
                <p><strong>Output (see inspector):</strong>
                    <script>
                        $(document).ready(function() {
                            var myVariable = "${g.forJavaScript(data: cookieStealingUrl)}";
                            $('#jsonInScriptBadExampleButton3').bind('click', function () {
                                location.href = myVariable;
                                // alert(myVariable);
                            });
                        });
                    </script>
                </p>
            </div>
            <div class="well">
                <p>How we do it at <span class="label label-success">MATCHi</span></p>
                <p><strong>Generating url with createLink with a parameter trying to steal cookie:</strong></p>
                <p><strong>Code:</strong><code> ${"var myVariable = \"\${g.forJavaScript(data: g.createLink(action: \"home\", controller: \"userProfile\", params: [param1: '\" + document.cookie'], absolute: true))};\""}</code></p>
                <p><button id="jsonInScriptGoodExampleButton4" class="btn btn-success">Test (should work with redirection)</button></p>
                <p><strong>Output (see inspector):</strong>
                    <script>
                        $(document).ready(function() {
                            var myVariable = "${g.forJavaScript(data: g.createLink(action: "home", controller: "userProfile", params: [param1: '" + document.cookie'], absolute: true))}";
                            $('#jsonInScriptGoodExampleButton4').bind('click', function () {
                                location.href = myVariable;
                            });
                        });
                    </script>
                </p>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-xs-12">
            <h3>CreateLink</h3>
            <p>Has same vulnerability as the a-tag used directly:</p>
            <div class="well">
                <p><strong>Danger data:</strong><code> ${"<script>alert('Dangerous script');</script>"} </code></p>
                <p><strong>Code:</strong><code> ${"<a href=\"<g:createLink uri=\"\${dangerTagData}\" />\">Hover this link</a>"}</code></p>
                <p><strong>Output:</strong> <a href="<g:createLink uri="${dangerTagData}" />">Hover this link</a></p>
            </div>
            <div class="well">
                <p><strong>Danger data:</strong><code> ${"<script>alert('Dangerous script');</script>"} </code></p>
                <p><strong>Code:</strong><code> ${"<a href=\"\${createLink(uri: dangerTagData)}\">Hover this link</a>"}</code></p>
                <p><strong>Output:</strong> <a href="${createLink(uri: dangerTagData)}">Hover this link</a></p>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-xs-12">
            <h3>Tooltips and tag attributes with html inside</h3>
            <p>In bootstrap 2, it is dangerous by default. In bootstrap 3, you need to set data-html to true for it to be dangerous.</p>
            <div class="well">
                <p><strong>Danger data:</strong><code> ${"<strong>This is ssome änna dåva HTML åså<script>alert('Danger!');</script></strong>"} </code></p>
                <p><strong>Code:</strong><code> ${"<a rel=\"tooltip\" data-html=\"true\" title=\"\${dangerHTML}\">Tooltip</a>"}</code></p>
                <p><strong>Output:</strong> <a rel="tooltip" data-html="true" title="${dangerHTML}">Tooltip</a></p>
                <script>
                    $(document).ready(function() {
                        $('[rel=tooltip]').tooltip();
                    });
                </script>
            </div>
            <div class="well">
                <p>How we do it at <span class="label label-success">MATCHi</span></p>
                <p><strong>Danger data:</strong><code> ${"<strong>This is ssome änna dåva HTML åså<script>alert('Danger!');</script></strong>"} </code></p>
                <p><strong>Code:</strong><code> ${"<a rel=\"tooltip\" data-html=\"true\" title=\"\${g.toRichHTML(text: dangerHTML)}\">Tooltip</a>"}</code></p>
                <p><strong>Output:</strong> <a rel="tooltip" data-html="true" title="${g.toRichHTML(text: dangerHTML)}">Tooltip</a></p>
                <script>
                    $(document).ready(function() {
                        $('[rel=tooltip]').tooltip();
                    });
                </script>
            </div>
            <div class="well">
                <p>How we do it at <span class="label label-success">MATCHi</span></p>
                <p><strong>Danger data:</strong><code> " onclick="javascript:alert("WOHO");" data-tennis=" </code></p>
                <p><strong>Code:</strong><code> ${"<a rel=\"tooltip\" data-html=\"true\" title=\"\${g.toRichHTML(text: dangerTagData2)}\">Tooltip</a>"}</code></p>
                <p><strong>Output:</strong> <a rel="tooltip" data-html="true" title="${g.toRichHTML(text: dangerTagData2)}">Tooltip</a></p>
                <script>
                    $(document).ready(function() {
                        $('[rel=tooltip]').tooltip();
                    });
                </script>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-xs-12">
            <h3>Rules of thumb for understanding</h3>
            <ul>
                <li>The set default encoding, which is the HTML codec, means that all <code>${"\${}"}</code> encodes according to that. It corresponds to using <code>.encodeAsHTML()</code> if there was no default encoding. </li>
                <li>Inside HTML tags, using <code>${"\${}"}</code> for attributes does not offer any protection since the string encoded string will be perfectly fit to be executed.</li>
                <li>Inside JavaScript tags, using <code>${"\${}"}</code> usually offers no protection, since the variable might contain stuff affecting the code.</li>
            </ul>
        </div>
    </div>
    <div class="row">
        <div class="col-xs-12">
            <h3>Regarding links</h3>
            <ul>
                <li>Always use a link generator such as createLink/link tags/helpers, since parameters seem to be okay. </li>
                <li>Only when the user themselves supply urls we have a real problem here. One example is facility homepage. Seems to be validated when saving though, needs to be verified it's the only place.</li>
            </ul>
        </div>
    </div>
    <div class="row">
        <div class="col-xs-12">
            <h3>Regarding tag attributes</h3>
            <ul>
                <li>Data attributes seems to be fine</li>
                <li>What is dangerous is when runnable attributes contain executable JavaScript, such as event handlers and urls.</li>
            </ul>
        </div>
    </div>
    <div class="row">
        <div class="col-xs-12">
            <h3>Left to check</h3>
            <ul>
                <li>AJAX? Should be solved by solving this in general. But let's find some places.</li>
            </ul>
        </div>
    </div>
    <div class="row">
        <div class="col-xs-12">
            <h3>Not listed use cases, since unlikely?</h3>
            <ul>
                <li>Rich HTML in JavaScript</li>
                <li>Rich HTML in tag attributes</li>
                <li>JSON objects in plain/presentable HTML</li>
            </ul>
        </div>
    </div>
</div>

</body>
</html>