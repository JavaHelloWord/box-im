<template>
	<view class="page chat-box">
		<view class="header">
			<text class="title">{{title}}</text>
			<uni-icons class="btn-side right" type="more-filled" size="30" @click="onShowMore()"></uni-icons>
		</view>
		<view class="chat-msg" @click="switchChatTabBox('none',true)">
			<scroll-view class="scroll-box" scroll-y="true" upper-threshold="200" @scrolltoupper="onScrollToTop"
				:scroll-into-view="'chat-item-'+scrollMsgIdx">
				<view v-if="chat" v-for="(msgInfo,idx) in chat.messages" :key="idx">
					<chat-message-item v-if="idx>=showMinIdx" :headImage="headImage(msgInfo)" @call="onRtCall(msgInfo)"
						:showName="showName(msgInfo)" @recall="onRecallMessage" @copy="onCopyMessage"
						@delete="onDeleteMessage" @longPressHead="onLongPressHead(msgInfo)" @download="onDownloadFile"
						:id="'chat-item-'+idx" :msgInfo="msgInfo" :groupMembers="groupMembers">
					</chat-message-item>
				</view>
			</scroll-view>
		</view>
		<view v-if="atUserIds.length>0" class="chat-at-bar" @click="openAtBox()">
			<view class="iconfont icon-at">:&nbsp;</view>
			<scroll-view v-if="atUserIds.length>0" class="chat-at-scroll-box" scroll-x="true" scroll-left="120">
				<view class="chat-at-items">
					<view v-for="m in atUserItems" class="chat-at-item">
						<head-image :name="m.showNickName" :url="m.headImage" :size="50"></head-image>
					</view>
				</view>
			</scroll-view>
		</view>
		<view class="send-bar">
			<view v-if="!showRecord" class="iconfont icon-voice-circle" @click="onRecorderInput()"></view>
			<view v-else class="iconfont icon-keyboard" @click="onKeyboardInput()"></view>
			<chat-record v-if="showRecord" class="chat-record" @send="onSendRecord"></chat-record>
			<view v-else class="send-text">
				<textarea class="send-text-area" v-model="sendText" auto-height :show-confirm-bar="false"
					:placeholder="isReceipt?'[回执消息]':''" :adjust-position="false" @confirm="sendTextMessage()"
					@keyboardheightchange="onKeyboardheightchange" @input="onTextInput" confirm-type="send" confirm-hold
					:hold-keyboard="true"></textarea>
			</view>
			<view v-if="chat && chat.type=='GROUP'" class="iconfont icon-at" @click="openAtBox()"></view>
			<view class="iconfont icon-icon_emoji" @click="onShowEmoChatTab()"></view>
			<view v-if="sendText==''" class="iconfont icon-add" @click="onShowToolsChatTab()">
			</view>
			<button v-if="sendText!=''||atUserIds.length>0" class="btn-send" type="primary"
				@touchend.prevent="sendTextMessage()" size="mini">发送</button>
		</view>

		<view class="chat-tab-bar" v-show="chatTabBox!='none' || (showKeyBoard && !isH5) " :style="{height:`${keyboardHeight}px`}">
			<view v-if="chatTabBox == 'tools'" class="chat-tools">
				<view class="chat-tools-item">
					<image-upload :maxCount="9" sourceType="album" :onBefore="onUploadImageBefore"
						:onSuccess="onUploadImageSuccess" :onError="onUploadImageFail">
						<view class="tool-icon iconfont icon-picture"></view>
					</image-upload>
					<view class="tool-name">相册</view>
				</view>
				<view class="chat-tools-item">
					<image-upload sourceType="camera" :onBefore="onUploadImageBefore" :onSuccess="onUploadImageSuccess"
						:onError="onUploadImageFail">
						<view class="tool-icon iconfont icon-camera"></view>
					</image-upload>
					<view class="tool-name">拍摄</view>
				</view>

				<view class="chat-tools-item">
					<file-upload :onBefore="onUploadFileBefore" :onSuccess="onUploadFileSuccess"
						:onError="onUploadFileFail">
						<view class="tool-icon iconfont icon-folder"></view>
					</file-upload>
					<view class="tool-name">文件</view>
				</view>

				<view class="chat-tools-item" @click="onRecorderInput()">
					<view class="tool-icon iconfont icon-microphone"></view>
					<view class="tool-name">语音消息</view>
				</view>
				<view v-if="chat.type == 'GROUP'" class="chat-tools-item" @click="switchReceipt()">
					<view class="tool-icon iconfont icon-receipt" :class="isReceipt?'active':''"></view>
					<view class="tool-name">回执消息</view>
				</view>
				<!-- #ifndef MP-WEIXIN -->
				<!-- 音视频不支持小程序 -->
				<view v-if="chat.type == 'PRIVATE'" class="chat-tools-item" @click="onPriviteVideo()">
					<view class="tool-icon iconfont icon-video"></view>
					<view class="tool-name">视频通话</view>
				</view>
				<view v-if="chat.type == 'PRIVATE'" class="chat-tools-item" @click="onPriviteVoice()">
					<view class="tool-icon iconfont icon-call"></view>
					<view class="tool-name">语音通话</view>
				</view>
				<view v-if="chat.type == 'GROUP'" class="chat-tools-item" @click="onGroupVideo()">
					<view class="tool-icon iconfont icon-call"></view>
					<view class="tool-name">语音通话</view>
				</view>
				<!-- #endif -->
			</view>
			<scroll-view v-if="chatTabBox==='emo'" class="chat-emotion" scroll-y="true">
				<view class="emotion-item-list">
					<image class="emotion-item" :title="emoText" :src="$emo.textToPath(emoText)"
						v-for="(emoText, i) in $emo.emoTextList" :key="i" @click="selectEmoji(emoText)" mode="aspectFit"
						lazy-load="true"></image>
				</view>
			</scroll-view>
			<view v-if="showKeyBoard"></view>
		</view>
		<!-- @用户时选择成员 -->
		<chat-at-box ref="atBox" :ownerId="group.ownerId" :members="groupMembers"
			@complete="onAtComplete"></chat-at-box>
		<!-- 群语音通话时选择成员 -->
		<!-- #ifndef MP-WEIXIN -->
		<group-member-selector ref="selBox" :members="groupMembers" :maxSize="configStore.webrtc.maxChannel"
			@complete="onInviteOk"></group-member-selector>
		<group-rtc-join ref="rtcJoin" :groupId="group.id"></group-rtc-join>
		<!-- #endif -->
	</view>
</template>

<script>
	import UNI_APP from '@/.env.js';

	export default {
		data() {
			return {
				chat: {},
				friend: {},
				group: {},
				groupMembers: [],
				sendText: "",
				isReceipt: false, // 是否回执消息
				scrollMsgIdx: 0, // 滚动条定位为到哪条消息
				chatTabBox: 'none',
				showKeyBoard: false,
				showRecord: false,
				keyboardHeight: 322,
				atUserIds: [],
				needScrollToBottom: false, // 需要滚动到底部 
				showMinIdx: 0, // 下标小于showMinIdx的消息不显示，否则可能很卡
				reqQueue: [], // 请求队列
				isSending: false, // 是否正在发送请求
				isH5: false  // h5键盘会强制推起页面，有些地方要区别对待
			}
		},
		methods: {
			onRecorderInput() {
				this.showRecord = true;
				this.switchChatTabBox('none', true);
			},
			onKeyboardInput() {
				this.showRecord = false;
				this.switchChatTabBox('none', false);
			},
			onSendRecord(data) {
				let msgInfo = {
					content: JSON.stringify(data),
					type: this.$enums.MESSAGE_TYPE.AUDIO,
					receipt: this.isReceipt
				}
				// 填充对方id
				this.fillTargetId(msgInfo, this.chat.targetId);
				this.sendMessageRequest(msgInfo).then((m) => {
					m.selfSend = true;
					this.chatStore.insertMessage(m);
					// 会话置顶
					this.moveChatToTop();
					// 滚动到底部
					this.scrollToBottom();
					this.isReceipt = false;

				})
			},
			onRtCall(msgInfo) {
				if (msgInfo.type == this.$enums.MESSAGE_TYPE.ACT_RT_VOICE) {
					this.onPriviteVoice();
				} else if (msgInfo.type == this.$enums.MESSAGE_TYPE.ACT_RT_VIDEO) {
					this.onPriviteVideo();
				}
			},
			onPriviteVideo() {
				const friendInfo = encodeURIComponent(JSON.stringify(this.friend));
				uni.navigateTo({
					url: `/pages/chat/chat-private-video?mode=video&friend=${friendInfo}&isHost=true`
				})
			},
			onPriviteVoice() {
				const friendInfo = encodeURIComponent(JSON.stringify(this.friend));
				uni.navigateTo({
					url: `/pages/chat/chat-private-video?mode=voice&friend=${friendInfo}&isHost=true`
				})
			},
			onGroupVideo() {
				// 邀请成员发起通话
				let ids = [this.mine.id];
				this.$refs.selBox.init(ids, ids);
				this.$refs.selBox.open();
			},
			onInviteOk(ids) {
				if (ids.length < 2) {
					return;
				}
				let users = [];
				ids.forEach(id => {
					let m = this.groupMembers.find(m => m.userId == id);
					// 只取部分字段,压缩url长度
					users.push({
						id: m.userId,
						nickName: m.showNickName,
						headImage: m.headImage,
						isCamera: false,
						isMicroPhone: true
					})
				})
				const groupId = this.group.id;
				const inviterId = this.mine.id;
				const userInfos = encodeURIComponent(JSON.stringify(users));
				uni.navigateTo({
					url: `/pages/chat/chat-group-video?groupId=${groupId}&isHost=true
						&inviterId=${inviterId}&userInfos=${userInfos}`
				})
			},
			moveChatToTop() {
				let chatIdx = this.chatStore.findChatIdx(this.chat);
				this.chatStore.moveTop(chatIdx);
			},
			switchReceipt() {
				this.isReceipt = !this.isReceipt;
			},
			openAtBox() {
				this.$refs.atBox.init(this.atUserIds);
				this.$refs.atBox.open();
			},
			onAtComplete(atUserIds) {
				this.atUserIds = atUserIds;
			},
			onLongPressHead(msgInfo) {
				if (!msgInfo.selfSend && this.chat.type == "GROUP" && this.atUserIds.indexOf(msgInfo.sendId) < 0) {
					this.atUserIds.push(msgInfo.sendId);
				}
			},
			headImage(msgInfo) {
				if (this.chat.type == 'GROUP') {
					let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
					return member ? member.headImage : "";
				} else {
					return msgInfo.selfSend ? this.mine.headImageThumb : this.chat.headImage
				}
			},
			showName(msgInfo) {
				if (this.chat.type == 'GROUP') {
					let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
					return member ? member.showNickName : "";
				} else {
					return msgInfo.selfSend ? this.mine.nickName : this.chat.showName
				}
			},
			sendTextMessage() {
				if (!this.sendText.trim() && this.atUserIds.length == 0) {
					return uni.showToast({
						title: "不能发送空白信息",
						icon: "none"
					});
				}
				let receiptText = this.isReceipt ? "【回执消息】" : "";
				let atText = this.createAtText();
				let msgInfo = {
					content: receiptText + this.sendText + atText,
					atUserIds: this.atUserIds,
					receipt: this.isReceipt,
					type: 0
				}
				this.sendText = "";
				// 填充对方id
				this.fillTargetId(msgInfo, this.chat.targetId);
				this.sendMessageRequest(msgInfo).then((m) => {
					m.selfSend = true;
					this.chatStore.insertMessage(m);
					// 会话置顶
					this.moveChatToTop();
				}).finally(() => {
					// 滚动到底部
					this.scrollToBottom();
					// 清空@用户列表
					this.atUserIds = [];
					this.isReceipt = false;
				});
			},
			createAtText() {
				let atText = "";
				this.atUserIds.forEach((id) => {
					if (id == -1) {
						atText += ` @全体成员`;
					} else {
						let member = this.groupMembers.find((m) => m.userId == id);
						if (member) {
							atText += ` @${member.showNickName}`;
						}
					}
				})
				return atText;
			},
			fillTargetId(msgInfo, targetId) {
				if (this.chat.type == "GROUP") {
					msgInfo.groupId = targetId;
				} else {
					msgInfo.recvId = targetId;
				}
			},
			scrollToBottom() {
				let size = this.messageSize;
				if (size > 0) {
					this.scrollToMsgIdx(size - 1);
				}
			},
			scrollToMsgIdx(idx) {
				// 如果scrollMsgIdx值没变化，滚动条不会移动
				if (idx == this.scrollMsgIdx && idx > 0) {
					this.$nextTick(() => {
						// 先滚动到上一条
						this.scrollMsgIdx = idx - 1;
						// 再滚动目标位置
						this.scrollToMsgIdx(idx);
					});
					return;
				}
				this.$nextTick(() => {
					this.scrollMsgIdx = idx;
				});

			},
			onShowEmoChatTab() {
				this.showRecord = false;
				this.switchChatTabBox('emo', true)
			},
			onShowToolsChatTab() {
				this.showRecord = false;
				this.switchChatTabBox('tools', true)
			},
			switchChatTabBox(chatTabBox, hideKeyBoard) {
				this.chatTabBox = chatTabBox;
				if (hideKeyBoard) {
					uni.hideKeyboard();
					this.showKeyBoard = false;
				}
			},
			selectEmoji(emoText) {
				this.sendText += `#${emoText};`;
			},
			onKeyboardheightchange(e) {
				if (e.detail.height > 0) {
					this.showKeyBoard = true;
					this.switchChatTabBox('none', false)
					this.keyboardHeight = this.rpxTopx(e.detail.height);
					this.scrollToBottom();
				} else {
					this.showKeyBoard = false;
				}
			},
			onUploadImageBefore(file) {
				let data = {
					originUrl: file.path,
					thumbUrl: file.path
				}
				let msgInfo = {
					id: 0,
					tmpId: this.generateId(),
					fileId: file.uid,
					sendId: this.mine.id,
					content: JSON.stringify(data),
					sendTime: new Date().getTime(),
					selfSend: true,
					type: this.$enums.MESSAGE_TYPE.IMAGE,
					readedCount: 0,
					loadStatus: "loading",
					status: this.$enums.MESSAGE_STATUS.UNSEND
				}
				// 填充对方id
				this.fillTargetId(msgInfo, this.chat.targetId);
				// 插入消息
				this.chatStore.insertMessage(msgInfo);
				// 会话置顶
				this.moveChatToTop();
				// 借助file对象保存
				file.msgInfo = msgInfo;
				// 滚到最低部
				this.scrollToBottom();
				return true;
			},
			onUploadImageSuccess(file, res) {
				let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
				msgInfo.content = JSON.stringify(res.data);
				msgInfo.receipt = this.isReceipt
				this.sendMessageRequest(msgInfo).then((m) => {
					msgInfo.loadStatus = 'ok';
					msgInfo.id = m.id;
					this.isReceipt = false;
					this.chatStore.insertMessage(msgInfo);
				})
			},
			onUploadImageFail(file, err) {
				let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
				msgInfo.loadStatus = 'fail';
				this.chatStore.insertMessage(msgInfo);
			},
			onUploadFileBefore(file) {
				let data = {
					name: file.name,
					size: file.size,
					url: file.path
				}
				let msgInfo = {
					id: 0,
					tmpId: this.generateId(),
					sendId: this.mine.id,
					content: JSON.stringify(data),
					sendTime: new Date().getTime(),
					selfSend: true,
					type: this.$enums.MESSAGE_TYPE.FILE,
					readedCount: 0,
					loadStatus: "loading",
					status: this.$enums.MESSAGE_STATUS.UNSEND
				}
				// 填充对方id
				this.fillTargetId(msgInfo, this.chat.targetId);
				// 插入消息
				this.chatStore.insertMessage(msgInfo);
				// 会话置顶
				this.moveChatToTop();
				// 借助file对象保存
				file.msgInfo = msgInfo;
				// 滚到最低部
				this.scrollToBottom();
				return true;
			},
			onUploadFileSuccess(file, res) {
				let data = {
					name: file.name,
					size: file.size,
					url: res.data
				}
				let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
				msgInfo.content = JSON.stringify(data);
				msgInfo.receipt = this.isReceipt
				this.sendMessageRequest(msgInfo).then((m) => {
					msgInfo.loadStatus = 'ok';
					msgInfo.id = m.id;
					this.isReceipt = false;
					this.chatStore.insertMessage(msgInfo);
				})
			},
			onUploadFileFail(file, res) {
				let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
				msgInfo.loadStatus = 'fail';
				this.chatStore.insertMessage(msgInfo);
			},
			onDeleteMessage(msgInfo) {
				uni.showModal({
					title: '删除消息',
					content: '确认删除消息?',
					success: (res) => {
						if (!res.cancel) {
							this.chatStore.deleteMessage(msgInfo);
							uni.showToast({
								title: "删除成功",
								icon: "none"
							})
						}
					}
				})
			},
			onRecallMessage(msgInfo) {
				uni.showModal({
					title: '撤回消息',
					content: '确认撤回消息?',
					success: (res) => {
						if (!res.cancel) {
							let url = `/message/${this.chat.type.toLowerCase()}/recall/${msgInfo.id}`
							this.$http({
								url: url,
								method: 'DELETE'
							}).then(() => {
								msgInfo = JSON.parse(JSON.stringify(msgInfo));
								msgInfo.type = this.$enums.MESSAGE_TYPE.RECALL;
								msgInfo.content = '你撤回了一条消息';
								msgInfo.status = this.$enums.MESSAGE_STATUS.RECALL;
								this.chatStore.insertMessage(msgInfo);
							})
						}
					}
				})
			},
			onCopyMessage(msgInfo) {
				uni.setClipboardData({
					data: msgInfo.content,
					success: () => {
						uni.showToast({ title: '已复制', icon: 'none' });
					},
					fail: () => {
						uni.showToast({ title: '复制失败', icon: 'none' });
					}
				});
			},
			onDownloadFile(msgInfo) {
				let url = JSON.parse(msgInfo.content).url;
				uni.downloadFile({
					url: url,
					success(res) {
						if (res.statusCode === 200) {
							var filePath = encodeURI(res.tempFilePath);
							uni.openDocument({
								filePath: filePath,
								showMenu: true
							});
						}
					},
					fail(e) {
						uni.showToast({
							title: "文件下载失败",
							icon: "none"
						})
					}
				});
			},
			onScrollToTop() {
				if (this.showMinIdx == 0) {
					console.log("消息已滚动到顶部")
					return;
				}
				//  #ifndef H5
				// 防止滚动条定格在顶部，不能一直往上滚
				this.scrollToMsgIdx(this.showMinIdx);
				// #endif
				// 多展示0条信息
				this.showMinIdx = this.showMinIdx > 20 ? this.showMinIdx - 20 : 0;
			},
			onShowMore() {
				if (this.chat.type == "GROUP") {
					uni.navigateTo({
						url: "/pages/group/group-info?id=" + this.group.id
					})
				} else {
					uni.navigateTo({
						url: "/pages/common/user-info?id=" + this.friend.id
					})
				}
			},
			onTextInput(e) {
				let idx = e.detail.cursor - 1;
				if (this.chat.type == 'GROUP' && e.detail.value[idx] == '@') {
					this.openAtBox();
					let sendText = e.detail.value.replace("@", '');
					this.$nextTick(() => {
						this.sendText = sendText;
					})
				}
			},
			loadReaded(fid) {
				this.$http({
					url: `/message/private/maxReadedId?friendId=${fid}`,
					method: 'get'
				}).then((id) => {
					this.chatStore.readedMessage({
						friendId: fid,
						maxId: id
					});
				});
			},
			readedMessage() {
				if (this.unreadCount == 0) {
					return;
				}
				let url = ""
				if (this.chat.type == "GROUP") {
					url = `/message/group/readed?groupId=${this.chat.targetId}`
				} else {
					url = `/message/private/readed?friendId=${this.chat.targetId}`
				}
				this.$http({
					url: url,
					method: 'PUT'
				}).then(() => {
					this.chatStore.resetUnreadCount(this.chat)
					this.scrollToBottom();
				})
			},
			loadGroup(groupId) {
				this.$http({
					url: `/group/find/${groupId}`,
					method: 'GET'
				}).then((group) => {
					this.group = group;
					this.chatStore.updateChatFromGroup(group);
					this.groupStore.updateGroup(group);
				});

				this.$http({
					url: `/group/members/${groupId}`,
					method: 'GET'
				}).then((groupMembers) => {
					this.groupMembers = groupMembers;
				});
			},
			loadFriend(friendId) {
				// 获取对方最新信息
				this.$http({
					url: `/user/find/${friendId}`,
					method: 'GET'
				}).then((friend) => {
					this.friend = friend;
					this.chatStore.updateChatFromFriend(friend);
					this.friendStore.updateFriend(friend);
				})
			},
			rpxTopx(rpx) {
				// px转换成rpx
				let info = uni.getSystemInfoSync()
				let px = info.windowWidth * rpx / 750;
				return Math.floor(rpx);
			},
			sendMessageRequest(msgInfo) {
				return new Promise((resolve, reject) => {
					// 请求入队列，防止请求"后发先至"，导致消息错序
					this.reqQueue.push({ msgInfo, resolve, reject });
					this.processReqQueue();
				})
			},
			processReqQueue() {
				if (this.reqQueue.length && !this.isSending) {
					this.isSending = true;
					const reqData = this.reqQueue.shift();
					this.$http({
						url: this.messageAction,
						method: 'post',
						data: reqData.msgInfo
					}).then((res) => {
						reqData.resolve(res)
					}).catch((e) => {
						reqData.reject(e)
					}).finally(() => {
						this.isSending = false;
						// 发送下一条请求
						this.processReqQueue();
					})
				}
			},
			listenKeyBoardForH5() {
				// 由于H5无法触发TextArea的@keyboardheightchange事件，所以通过
				// 监听屏幕高度变化来实现键盘监听
				let initHeight = window.innerHeight;
				window.addEventListener('resize', () => {
					let keyboardHeight = initHeight - window.innerHeight;
					if (keyboardHeight > 0) {
						this.keyboardHeight = keyboardHeight - 20;
						this.showKeyBoard = true;
						this.switchChatTabBox('none', false)
						this.scrollToBottom();
					} else {
						this.showKeyBoard = false;
					}
				});
			},
			generateId() {
				// 生成临时id
				return String(new Date().getTime()) + String(Math.floor(Math.random() * 1000));
			}
		},
		computed: {
			mine() {
				return this.userStore.userInfo;
			},
			title() {
				if (!this.chat) {
					return "";
				}
				let title = this.chat.showName;
				if (this.chat.type == "GROUP") {
					let size = this.groupMembers.filter(m => !m.quit).length;
					title += `(${size})`;
				}
				return title;
			},
			messageAction() {
				return `/message/${this.chat.type.toLowerCase()}/send`;
			},
			messageSize() {
				if (!this.chat || !this.chat.messages) {
					return 0;
				}
				return this.chat.messages.length;
			},
			unreadCount() {
				if (!this.chat || !this.chat.unreadCount) {
					return 0;
				}
				return this.chat.unreadCount;
			},
			atUserItems() {
				let atUsers = [];
				this.atUserIds.forEach((id) => {
					if (id == -1) {
						atUsers.push({
							id: -1,
							showNickName: "全体成员"
						})
						return;
					}
					let member = this.groupMembers.find((m) => m.userId == id);
					if (member) {
						atUsers.push(member);
					}
				})
				return atUsers;
			}
		},
		watch: {
			messageSize: function(newSize, oldSize) {
				// 接收到消息时滚动到底部
				if (newSize > oldSize) {
					let pages = getCurrentPages();
					let curPage = pages[pages.length - 1].route;
					if (curPage == "pages/chat/chat-box") {
						this.scrollToBottom();
					} else {
						this.needScrollToBottom = true;
					}
				}
			},
			unreadCount: {
				handler(newCount, oldCount) {
					if (newCount > 0) {
						// 消息已读
						this.readedMessage()
					}
				}
			}
		},
		onLoad(options) {
			// #ifdef H5
				this.isH5 = true;
				this.listenKeyBoardForH5();
			// #endif
			// 聊天数据
			this.chat = this.chatStore.chats[options.chatIdx];
			// 初始状态只显示20条消息
			let size = this.messageSize;
			this.showMinIdx = size > 20 ? size - 20 : 0;
			// 消息已读
			this.readedMessage()
			// 加载好友或群聊信息
			if (this.chat.type == "GROUP") {
				this.loadGroup(this.chat.targetId);
			} else {
				this.loadFriend(this.chat.targetId);
				this.loadReaded(this.chat.targetId)
			}
			// 激活当前会话
			this.chatStore.activeChat(options.chatIdx);
			// 复位回执消息
			this.isReceipt = false;
		},
		onShow() {
			if (this.needScrollToBottom) {
				// 页面滚到底部
				this.scrollToBottom();
				this.needScrollToBottom = false;
			}
		}
	}
</script>

<style lang="scss" scoped>
	.chat-box {
		position: relative;
		border: #dddddd solid 1px;
		display: flex;
		flex-direction: column;

		.header {
			display: flex;
			justify-content: center;
			align-items: center;
			height: 60rpx;
			padding: 5px;
			background-color: #f8f8f8;
			line-height: 50px;
			font-size: 36rpx;
			border: #dddddd solid 1px;


			.btn-side {
				position: absolute;
				line-height: 60rpx;
				font-size: 28rpx;
				cursor: pointer;

				&.right {
					right: 30rpx;
				}
			}
		}

		.chat-msg {
			flex: 1;
			padding: 0;
			border: #dddddd solid 1px;
			overflow: hidden;
			position: relative;
			background-color: white;

			.scroll-box {
				height: 100%;
			}
		}

		.chat-at-bar {
			display: flex;
			align-items: center;
			padding: 0 10rpx;
			border: #dddddd solid 1px;

			.icon-at {
				font-size: 35rpx;
				color: darkblue;
				font-weight: 600;
			}

			.chat-at-scroll-box {
				flex: 1;
				width: 80%;

				.chat-at-items {
					display: flex;
					align-items: center;
					height: 70rpx;

					.chat-at-item {
						padding: 0 3rpx;
					}
				}
			}

		}

		.send-bar {
			display: flex;
			align-items: center;
			padding: 10rpx;
			margin-bottom: 10rpx;
			border: #dddddd solid 1px;
			background-color: #f7f8fd;
			height: 80rpx;

			.iconfont {
				font-size: 68rpx;
				margin: 6rpx;
			}

			.chat-record {
				flex: 1;

			}

			.send-text {
				flex: 1;
				overflow: auto;
				padding: 20rpx;
				background-color: #fff;
				border-radius: 20rpx;
				font-size: 30rpx;
				box-sizing: border-box;

				.send-text-area {
					width: 100%;
				}
			}

			.btn-send {
				margin: 5rpx;
			}
		}


		.chat-tab-bar {
			height: 500rpx;
			padding: 20rpx;
			background-color: #f8f8f8;

			.chat-tools {
				display: flex;
				flex-wrap: wrap;

				.chat-tools-item {
					width: 140rpx;
					padding: 16rpx;
					display: flex;
					flex-direction: column;
					align-items: center;

					.tool-icon {
						padding: 28rpx;
						font-size: 60rpx;
						border-radius: 20%;
						background-color: white;
						color: black;

						&.active {
							background-color: #ddd;
						}
					}

					.tool-name {
						height: 60rpx;
						line-height: 60rpx;
						font-size: 28rpx;
					}
				}
			}

			.chat-emotion {
				height: 100%;

				.emotion-item-list {
					display: flex;
					flex-wrap: wrap;

					.emotion-item {
						width: 40px;
						height: 40px;
						text-align: center;
						cursor: pointer;
						padding: 6px;
					}
				}
			}

		}
	}
</style>