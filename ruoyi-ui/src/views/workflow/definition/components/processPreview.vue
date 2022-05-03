<template>
  <el-dialog title="预览" :visible.sync="visible" align="center" width="70%" append-to-body>
      <el-image :src="url" v-if="type==='png'">
        <div slot="placeholder">流程图加载中 <i class="el-icon-loading"></i></div>
      </el-image>
      <div class="xml-data" v-if="type==='xml'">
        <div class="xml-data-line">
          <code class="xml-data-code" v-if="url" v-html="parseXML" />
       </div>
      </div>
  </el-dialog>
</template>

<script>
import 'highlight.js/styles/a11y-dark.css'
import Hljs from 'highlight.js'
export default {
    props: {
      url: String,
      type: String,
    },
    data() {
      return {
        visible: false,
        previewType: 'xml',
        width: '450px',
        height: '300px'
      }
    },
    computed: {
      parseXML() {
        /* const codeArry = []
        this.url.split('\n').forEach(lineCode => {
          codeArry.push(Hljs.highlightAuto(lineCode).value)
        }) */
        return Hljs.highlightAuto(this.url).value
      }
    },
}
</script>
<style>
.xml-data {
  background-color: #2B2B2B;
  border-radius: 5px;
  color: #C6C6C6;
  word-break:break-all;
  overflow-y: scroll;
  overflow-x: hidden;
  box-sizing: border-box;
  padding: 8px 0px;
  height: 500px;
  width: inherit;
}

/* 修改滚动条样式 */
.xml-data::-webkit-scrollbar {
	width: 4px;
}
.xml-data::-webkit-scrollbar-thumb {
	border-radius: 10px;
	-webkit-box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.2);
	background: rgba(198, 198, 198, 1);
}

.xml-data-line {
  line-height: 22px;
}

.xml-data-line-no {
  display: inline-block;
  width: 20px;
  font-size: 12px;
  margin-right: 15px;
  text-align: right;
  color: #858585;
}

.xml-data-code {
  color: #C5C8C6;
}
</style>
