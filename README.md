# MMBase Bridge Interface
<p>
MMBase Bridge is a part of MMBase, but can also be used in
other projects. Its main goal is to contain the 'bridge' api to talk to MMBase.
But it has a few other things too:
</p>
<ul>
 <li>'bridge': These things can be distinguished but are are actually very related to each other.
   <ul>
    <li>The Bridge API itself (org.mmbase.bridge)</li>
    <li>A SearchQuery abstraction and implementation (org.mmbase.storage.search)</li>
    <li>A DataType framework, plus a bunch if implementations (org.mmbase.datatypes).</li>
    <li>MMBase security API</li>
    </ul>
 </li>
 <li>A Portal framework (org.mmbase.framework)</li>
 <li>Utils to work with this bridge (org.mmbase.bridge.util)</li>
 <li>A mock implementation of this bridge (org.mmbase.bridge.mock)</li>
 <li>A caching framework. Could perhaps be (partially) moved to utils</li>
 <li>A function framework (org.mmbase.util.functions). This depends on DataTypes.</li>
</ul>
