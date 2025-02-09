<template>
	<el-container class="group-page">
		<el-aside width="280px" class="group-list-box">
			<div class="group-list-header">
				<el-input class="search-text" placeholder="搜索" v-model="searchText">
					<i class="el-icon-search el-input__icon" slot="prefix"> </i>
				</el-input>
				<el-button plain class="add-btn" icon="el-icon-plus" title="创建群聊" @click="onCreateGroup()"></el-button>
			</div>
			<el-scrollbar class="group-list-items">
				<div v-for="(group,index) in groupStore.groups" :key="index">
					<group-item v-show="!group.quit&&group.showGroupName.includes(searchText)" :group="group"
						:active="group === groupStore.activeGroup" @click.native="onActiveItem(group,index)">
					</group-item>
				</div>
			</el-scrollbar>
		</el-aside>
		<el-container class="group-box">
			<div class="group-header" v-show="activeGroup.id">
				{{activeGroup.showGroupName}}({{groupMembers.length}})
			</div>
			<el-scrollbar class="group-container">
				<div v-show="activeGroup.id">
					<div class="group-info">
						<div>
							<file-upload v-show="isOwner" class="avatar-uploader" :action="imageAction"
								:showLoading="true" :maxSize="maxSize" @success="onUploadSuccess"
								:fileTypes="['image/jpeg', 'image/png', 'image/jpg','image/webp']">
								<img v-if="activeGroup.headImage" :src="activeGroup.headImage" class="avatar">
								<i v-else class="el-icon-plus avatar-uploader-icon"></i>
							</file-upload>
							<head-image v-show="!isOwner" class="avatar" :size="200" :url="activeGroup.headImage"
								radius="10%" :name="activeGroup.showGroupName">
							</head-image>
							<el-button class="send-btn" icon="el-icon-position" type="primary"
								@click="onSendMessage()">发消息</el-button>
						</div>
						<el-form class="group-form" label-width="130px" :model="activeGroup" :rules="rules"
							ref="groupForm">
							<el-form-item label="群聊名称" prop="name">
								<el-input v-model="activeGroup.name" :disabled="!isOwner" maxlength="20"></el-input>
							</el-form-item>
							<el-form-item label="群主">
								<el-input :value="ownerName" disabled></el-input>
							</el-form-item>
							<el-form-item label="群名备注">
								<el-input v-model="activeGroup.remarkGroupName" :placeholder="activeGroup.name"
									maxlength="20"></el-input>
							</el-form-item>
							<el-form-item label="我在本群的昵称">
								<el-input v-model="activeGroup.remarkNickName" maxlength="20"
									:placeholder="$store.state.userStore.userInfo.nickName"></el-input>
							</el-form-item>
							<el-form-item label="群公告">
								<el-input v-model="activeGroup.notice" :disabled="!isOwner" type="textarea"
									maxlength="1024" placeholder="群主未设置"></el-input>
							</el-form-item>
							<div>
								<el-button type="success" @click="onSaveGroup()">保存</el-button>
								<el-button type="danger" v-show="!isOwner" @click="onQuit()">退出群聊</el-button>
								<el-button type="danger" v-show="isOwner" @click="onDissolve()">解散群聊</el-button>
							</div>
						</el-form>
					</div>
					<el-divider content-position="center"></el-divider>
					<el-scrollbar style="height:200px;">
						<div class="group-member-list">
							<div v-for="(member) in groupMembers" :key="member.id">
								<group-member v-show="!member.quit" class="group-member" :member="member"
									:showDel="isOwner&&member.userId!=activeGroup.ownerId" @del="onKick"></group-member>
							</div>
							<div class="group-invite">
								<div class="invite-member-btn" title="邀请好友进群聊" @click="onInviteMember()">
									<i class="el-icon-plus"></i>
								</div>
								<div class="invite-member-text">邀请</div>
								<add-group-member :visible="showAddGroupMember" :groupId="activeGroup.id"
									:members="groupMembers" @reload="loadGroupMembers"
									@close="onCloseAddGroupMember"></add-group-member>
							</div>
						</div>
					</el-scrollbar>
				</div>
			</el-scrollbar>
		</el-container>
	</el-container>
</template>


<script>
	import GroupItem from '../components/group/GroupItem';
	import FileUpload from '../components/common/FileUpload';
	import GroupMember from '../components/group/GroupMember.vue';
	import AddGroupMember from '../components/group/AddGroupMember.vue';
	import HeadImage from '../components/common/HeadImage.vue';
	export default {
		name: "group",
		components: {
			GroupItem,
			GroupMember,
			FileUpload,
			AddGroupMember,
			HeadImage
		},
		data() {
			return {
				searchText: "",
				maxSize: 5 * 1024 * 1024,
				activeGroup: {},
				groupMembers: [],
				showAddGroupMember: false,
				rules: {
					name: [{
						required: true,
						message: '请输入群聊名称',
						trigger: 'blur'
					}]
				}
			};
		},
		methods: {
			onCreateGroup() {
				this.$prompt('请输入群聊名称', '创建群聊', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					inputPattern: /\S/,
					inputErrorMessage: '请输入群聊名称'
				}).then((o) => {
					let userInfo = this.$store.state.userStore.userInfo;
					let data = {
						name: o.value
					}
					this.$http({
						url: `/group/create?groupName=${o.value}`,
						method: 'post',
						data: data
					}).then((group) => {
						this.$store.commit("addGroup", group);
					})
				})
			},
			onActiveItem(group, index) {
				this.$store.commit("activeGroup", index);
				// store数据不能直接修改，所以深拷贝一份内存
				this.activeGroup = JSON.parse(JSON.stringify(group));
				// 重新加载群成员
				this.loadGroupMembers();
			},
			onInviteMember() {
				this.showAddGroupMember = true;
			},
			onCloseAddGroupMember() {
				this.showAddGroupMember = false;
			},
			onUploadSuccess(data) {
				this.activeGroup.headImage = data.originUrl;
				this.activeGroup.headImageThumb = data.thumbUrl;
			},
			onSaveGroup() {
				this.$refs['groupForm'].validate((valid) => {
					if (valid) {
						let vo = this.activeGroup;
						this.$http({
							url: "/group/modify",
							method: "put",
							data: vo
						}).then((group) => {
							this.$store.commit("updateGroup", group);
							this.$message.success("修改成功");
						})
					}
				});
			},
			onDissolve() {
				this.$confirm(`确认要解散'${this.activeGroup.name}'吗?`, '确认解散?', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(() => {
					this.$http({
						url: `/group/delete/${this.activeGroup.id}`,
						method: 'delete'
					}).then(() => {
						this.$message.success(`群聊'${this.activeGroup.name}'已解散`);
						this.$store.commit("removeGroup", this.activeGroup.id);
						this.$store.commit("removeGroupChat", this.activeGroup.id);
						this.reset();
					});
				})

			},
			onKick(member) {
				this.$confirm(`确定将成员'${member.showNickName}'移出群聊吗？`, '确认移出?', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(() => {
					this.$http({
						url: `/group/kick/${this.activeGroup.id}`,
						method: 'delete',
						params: {
							userId: member.userId
						}
					}).then(() => {
						this.$message.success(`已将${member.showNickName}移出群聊`);
						member.quit = true;
					});
				})

			},
			onQuit() {
				this.$confirm(`确认退出'${this.activeGroup.showGroupName}',并清空聊天记录吗？`, '确认退出?', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(() => {
					this.$http({
						url: `/group/quit/${this.activeGroup.id}`,
						method: 'delete'
					}).then(() => {
						this.$message.success(`您已退出'${this.activeGroup.name}'`);
						this.$store.commit("removeGroup", this.activeGroup.id);
						this.$store.commit("removeGroupChat", this.activeGroup.id);
						this.reset();
					});
				})

			},
			onSendMessage() {
				let chat = {
					type: 'GROUP',
					targetId: this.activeGroup.id,
					showName: this.activeGroup.showGroupName,
					headImage: this.activeGroup.headImage,
				};
				this.$store.commit("openChat", chat);
				this.$store.commit("activeChat", 0);
				this.$router.push("/home/chat");
			},
			loadGroupMembers() {
				this.$http({
					url: `/group/members/${this.activeGroup.id}`,
					method: "get"
				}).then((members) => {
					this.groupMembers = members;
				})
			},
			reset() {
				this.activeGroup = {};
				this.groupMembers = [];
			}
		},
		computed: {
			groupStore() {
				return this.$store.state.groupStore;
			},
			ownerName() {
				let member = this.groupMembers.find((m) => m.userId == this.activeGroup.ownerId);
				return member && member.showNickName;
			},
			isOwner() {
				return this.activeGroup.ownerId == this.$store.state.userStore.userInfo.id;
			},
			imageAction() {
				return `/image/upload`;
			}
		}
	}
</script>

<style lang="scss">
	.group-page {
		.group-list-box {
			display: flex;
			flex-direction: column;
			border-right: #53a0e79c solid 1px;
			background: #F8F8F8;

			.group-list-header {
				height: 50px;
				display: flex;
				align-items: center;
				padding: 3px 8px;
				background-color: white;
				border-bottom: 1px #ddd solid;

				.el-input__inner {
					border-radius: 10px !important;
				}

				.add-btn {
					padding: 5px !important;
					margin: 5px;
					font-size: 20px;
					color: #587FF0;
					border: #587FF0 1px solid;
					background-color: #F0F8FF;
					border-radius: 50%;
				}
			}

			.group-list-items {
				flex: 1;
				margin: 0 3px;
				background: #F8F8F8;
			}
		}

		.group-box {
			display: flex;
			flex-direction: column;
			
			.group-header {
				padding: 3px;
				height: 50px;
				line-height: 50px;
				font-size: 20px;
				font-weight: 600;
				text-align: center;
				background-color: white;
				border-bottom: 1px #ddd solid;
			}

			.group-container {
				padding: 20px;
				flex: 1;

				.group-info {
					display: flex;
					padding: 5px 20px;

					.group-form {
						flex: 1;
						padding-left: 40px;
						max-width: 700px;
					}

					.avatar-uploader {
						text-align: left;

						.el-upload {
							border: 1px dashed #d9d9d9 !important;
							border-radius: 6px;
							cursor: pointer;
							position: relative;
							overflow: hidden;
						}

						.el-upload:hover {
							border-color: #409EFF;
						}

						.avatar-uploader-icon {
							font-size: 28px;
							color: #8c939d;
							width: 200px;
							height: 200px;
							line-height: 200px;
							text-align: center;
						}

						.avatar {
							width: 200px;
							height: 200px;
							display: block;
						}
					}

					.send-btn {
						margin-top: 20px;
					}
				}

				.group-member-list {
					padding: 5px 20px;
					display: flex;
					align-items: center;
					flex-wrap: wrap;
					font-size: 16px;
					text-align: center;

					.group-member {
						margin-right: 15px;
					}

					.group-invite {
						display: flex;
						flex-direction: column;
						align-items: center;
						width: 60px;

						.invite-member-btn {
							width: 100%;
							height: 60px;
							line-height: 60px;
							border: #cccccc solid 1px;
							font-size: 25px;
							cursor: pointer;
							box-sizing: border-box;

							&:hover {
								border: #aaaaaa solid 1px;
							}
						}

						.invite-member-text {
							font-size: 16px;
							text-align: center;
							width: 100%;
							height: 30px;
							line-height: 30px;
							white-space: nowrap;
							text-overflow: ellipsis;
							overflow: hidden
						}
					}

				}
			}
		}
	}
</style>