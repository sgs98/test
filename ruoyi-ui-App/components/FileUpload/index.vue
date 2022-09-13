<template>
  <view>
	<div class="upload-file">
		<uni-file-picker 
			v-model="fileList"
			file-mediatype="all"
			:auto-upload="false"
			:limit="limit"
			@progress="progress" 
			@success="handleUploadSuccess" 
			@fail="handleUploadError" 
			@select="handleSelect"
			@delete="handleDelete"
		/>
	</div>

	<!-- 提示信息弹窗 -->
	<uni-popup ref="message" type="message">
		<uni-popup-message :type="msgType" :message="messageText" :duration="2000"></uni-popup-message>
	</uni-popup>
	<!-- 提示窗 -->
	<uni-popup ref="alertDialog" type="dialog">
		<uni-popup-dialog :type="alertType" cancelText="取消" confirmText="确认" title="提示" :content="alertContent" @confirm="dialogConfirm"
			@close="dialogClose"></uni-popup-dialog>
	</uni-popup>
  </view>
</template>

<script>
import { getToken } from "@/utils/auth";
import { listByIds,delOss } from "@/api/system/oss";

export default {
  name: "FileUpload",
  props: {
    // 值
    ossIds: [String, Object, Array],
    // 数量限制
    limit: {
      type: Number,
      default: 5,
    },
    // 大小限制(MB)
    fileSize: {
      type: Number,
      default: 150,
    },
	// 文件类型
	fileType: {
	  type: Array,
	  default: () => ["doc", "xls", "ppt", "txt", "pdf","zip","rar",'png', 'jpg', 'jpeg'],
	},
  },
  data() {
    return {
		number: 0,
		baseUrl: getApp().globalData.config.baseUrl,
		uploadFileUrl: getApp().globalData.config.baseUrl + "/system/oss/upload", // 上传的图片服务器地址
		formData:{
			Authorization: "Bearer " + getToken(),
		},
		headers: {
			'content-type': 'multipart/form-data'
		},
		//上传列表
		uploadList:[],
		fileList: [],
	
		// 消息提示窗口、提示框
		msgType: '',
		messageText: '',
		// 弹出框
		alertType:'',
		alertContent:'',
		//待删除对象
		tempItem:undefined,
    };
  },
  watch: {
    ossIds: {
      async handler(val) {
        if (val) {
          let temp = 1;
          // 首先将值转为数组
          let list;
          if (Array.isArray(val)) {
            list = val;
          } else {
            await listByIds(val).then(res => {
				console.log(res)
              list = res.data.map(oss => {
                oss = { name: oss.originalName, url: oss.url, fileId: oss.ossId };
                return oss;
              });
            })
          }
          // 然后将数组转为对象数组
          this.fileList = list.map(item => {
            item = { name: item.name, url: item.url, fileId: item.ossId,extname:undefined,fileType:undefined,image:undefined,size:undefined  };
            item.path = item.path;
            return item;
          });
        } else {
          this.fileList = [];
          return [];
        }
      },
      deep: true,
      immediate: true
    }
  },
  computed: {
  },
  methods: {
	async handleSelect(res){// 上传
	  const app = this
	  const tempFilePaths = res.tempFilePaths;
	  uni.uploadFile({
		url: this.uploadFileUrl,
		filePath: tempFilePaths[0],
		name: 'file',
		formData:{
			Authorization: "Bearer " + getToken(),
		},
		headers: {
			'content-type': 'multipart/form-data'
		},
		success: (res) => {
			const resData = JSON.parse(res.data)
			if(res.statusCode != 200 ){
				this.msgType = 'error'
				this.messageText = res.errMsg
				this.$refs.message.open()
			}
			if (resData.code === 200) {
				this.uploadList.push({ name: resData.data.fileName, url: resData.data.url, fileId: resData.data.ossId });
				this.fileList = this.fileList.concat(this.uploadList);
				this.$emit("input", this.listToString(this.fileList));
				this.msgType = 'success'
				this.messageText = resData.msg
				this.$refs.message.open()
			} else {
				this.msgType = 'error'
				this.messageText = resData.msg
				this.$refs.message.open()
			}
		}
	  });
	},

    // 删除文件
    handleDelete(item) {
	  console.log(this.fileList)
      let ossId = item.tempFile.fileId;
	  this.tempItem = item;
	  this.alertType = 'warn'
	  this.alertContent ='是否确认删除OSS对象存储编号为"' + ossId + '"的数据项?'
	  this.$refs.alertDialog.open()
    },
	// 确认删除
	dialogConfirm(){
		if(this.tempItem){
			return delOss(this.tempItem.tempFile.fileId);
		}
		this.fileList.$remove(this.tempItem)
		this.$emit("input", this.listToString(this.fileList));
	},
	// 取消删除
	dialogClose(){
		console.log(this.tempItem.tempFile)
		this.fileList.push(this.tempItem.tempFile)
		console.log(this.fileList)
	},
    /** 下载文件 */
    handleDownload(index) {
      let ossId = this.fileList[index].ossId;
      this.$download.oss(ossId)
    },

    // 获取文件名称
    getFileName(name) {
      // 如果是url那么取最后的名字 如果不是直接返回
      if (name.lastIndexOf("/") > -1) {
        return name.slice(name.lastIndexOf("/") + 1);
      } else {
        return name;
      }
    },
    // 对象转成指定字符串分隔
    listToString(list, separator) {
      let strs = "";
      separator = separator || ",";
      for (let i in list) {
        strs += list[i].fileId + separator;
      }
      return strs != "" ? strs.substr(0, strs.length - 1) : "";
    },
  },
};
</script>

<style scoped lang="scss">
.upload-file-uploader {
  margin-bottom: 5px;
}
.upload-file-list .el-upload-list__item {
  border: 1px solid #e4e7ed;
  line-height: 2;
  margin-bottom: 10px;
  position: relative;
}
.upload-file-list .ele-upload-list__item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: inherit;
}
.ele-upload-list__item-content-action .el-link {
  margin-right: 10px;
}
</style>
