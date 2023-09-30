<template>
    <div class="account-index">
        <wrapper-content>
            <div class="action">
            </div>
            <a-table :columns="columns" :dataSource="dataSource" :loading="loading" rowKey="id" :pagination="pagination" @change="pageChange">
                <template slot="avatar" slot-scope="text,record,index">
                    <a-avatar :src="record.avatar" />
                </template>
                <template slot="status" slot-scope="text,record,index">
                    <span class="success-color" v-if="record.status">Using</span>
                    <span v-else>disabled</span>
                </template>
                <template slot="action" slot-scope="text,record,index">
                    <template v-if="!record.is_owner">
                        <a @click="rowClick(record,'edit')">edit</a>
                        <a-divider type="vertical"/>
                        <a @click="auth(record)">authorized</a>
                        <a-divider type="vertical"/>
                        <a @click="rowClick(record,'status')"><span v-if="record.status">disabled</span><span v-else>enable</span></a>
                        <a-divider type="vertical"/>
                        <a @click="rowClick(record,'del')">delete</a>
                    </template>
                </template>
            </a-table>
        </wrapper-content>
        <a-modal
                v-model="actionInfo.modalStatus"
                :width="800"
                :title="actionInfo.modalTitle"
                :bodyStyle="{paddingBottom:'1px'}"
                :footer="null"
        >
            <a-form
                    :autoFormCreate="(form)=>{this.form = form}"
                    @submit.prevent="handleSubmit"
            >
                <a-form-item
                        label='Name'
                        :labelCol="{ span: 3 }"
                        :wrapperCol="{ span: 21 }"
                        fieldDecoratorId="name"
                        :fieldDecoratorOptions="{rules: [{ required: true, message: 'Please type in your name' }]}"
                >
                    <a-input placeholder='Name'/>
                </a-form-item>
                <a-form-item
                        label='cell phone'
                        :labelCol="{ span: 3 }"
                        :wrapperCol="{ span: 21 }"
                        fieldDecoratorId="mobile"
                >
                    <a-input placeholder='phone number'/>
                </a-form-item>
                <a-form-item
                        label='Mail'
                        :labelCol="{ span: 3 }"
                        :wrapperCol="{ span: 21 }"
                        fieldDecoratorId="email"
                >
                    <a-input placeholder='email address'/>
                </a-form-item>
                <a-form-item
                        label='describe'
                        :labelCol="{ span: 3 }"
                        :wrapperCol="{ span: 21 }"
                        fieldDecoratorId="description"
                        help='Optional, set the remarks of the account'
                >
                    <a-text-area placeholder='account description'/>
                </a-form-item>
                <a-form-item
                        :wrapperCol="{ span: 21, offset: 3 }"
                >
                    <div class="action-btn">
                        <a-button type="primary" htmlType='submit'
                                  :loading="actionInfo.confirmLoading"
                                  class="middle-btn">save
                        </a-button>
                        <a-button class="middle-btn" @click="actionInfo.modalStatus = false">Cancel
                        </a-button>
                    </div>
                </a-form-item>
            </a-form>
        </a-modal>
        <a-modal
                v-model="authInfo.modalStatus"
                :width="800"
                title="user authorization"
                :bodyStyle="{paddingBottom:'1px'}"
                :footer="null"
        >
            <div v-if="currentUser" style="margin: 0 0 24px 0;">
                <div style="margin-bottom: 24px;">
                    <a-radio-group v-model="currentUser.authorize"  buttonStyle="solid">
                        <a-radio-button :value="auth.value" v-for="auth in authList" :key="auth.value">{{auth.label}}</a-radio-button>
                    </a-radio-group>
                    <!--<a-checkbox-group :options="authList" v-model="currentUser.authorizeArr"/>-->
                </div>
                <div class="action-btn">
                    <a-button type="primary"
                              @click="authApply"
                              :loading="authInfo.confirmLoading"
                              class="middle-btn">save
                    </a-button>
                    <a-button class="middle-btn" @click="authInfo.modalStatus = false">Cancel
                    </a-button>
                </div>
            </div>
        </a-modal>
    </div>
</template>
<script>
    import AInput from 'ant-design-vue/es/input'
    import {doAccount, forbid, list, resume, auth, del} from "../../../api/user";
    import {checkResponse} from "../../../assets/js/utils";
    import pagination from "@/mixins/pagination";

    const ATextArea = AInput.TextArea;
    const columns = [{
        title: 'avatar',
        dataIndex: 'avatar',
        width: '30%',
        scopedSlots: {
            customRender: 'avatar'
        }
    }, {
        title: 'Name',
        dataIndex: 'name',
        width: '15%',
    }, {
        title: 'cell phone',
        dataIndex: 'mobile',
        width: '15%',
    },{
        title: 'status',
        dataIndex: 'status',
        scopedSlots: {
            customRender: 'status'
        }
    }, {
        title: 'operate',
        scopedSlots: {
            customRender: 'action'
        }
    }];
    export default {
        components: {
            ATextArea,
        },
        mixins: [pagination],
        data() {
            return {
                columns,
                dataSource: [],
                loading: true,
                authList: [],
                newData: {
                    id: 0
                },
                form: {},
                currentUser: null,
                authInfo: {
                    modalStatus: false,
                    confirmLoading: false,
                },
                actionInfo: {
                    modalStatus: false,
                    confirmLoading: false,
                    modalTitle: 'edit account',
                    okText: 'save',
                    cancelText: 'quit',
                }
            }
        },
        created() {
            this.init();
        },
        methods: {
            init(){
                let app = this;
                app.loading = true;
                list(app.requestData).then(res=>{
                    app.dataSource = res.data.list;
                    app.pagination.total = res.data.total;
                    app.authList = [];
                    res.data.authList.forEach(function (v) {
                        const obj = {
                            label: v.title,
                            value: v.id
                        };
                        app.authList.push(obj);
                    });
                    app.loading = false;
                })
            },
            rowClick(record, action) {
                let app = this;
                app.newData = {id: 0};
                if (action == 'add' || action == 'edit' || action == 'new') {
                    setTimeout(function () {
                        app.form && app.form.resetFields();
                    }, 0);
                    app.actionInfo.modalStatus = true;
                    app.actionInfo.modalTitle = 'add account';
                    if (action == 'edit') {
                        //The modal sub-element will not be instantiated before the modal is displayed, and the processing will be delayed
                        setTimeout(function () {
                            app.actionInfo.modalTitle = 'edit account';
                            app.form.setFieldsValue({
                                name: record.name,
                                mobile: record.mobile,
                                email: record.email,
                                description: record.description,
                            });
                            app.newData = record;
                        }, 0);
                    }
                } else if (action == 'status') {
                    const status = record.status;
                    status ? forbid(record.code).then(res=>{
                        if (checkResponse(res)) {
                            record.status = Number(!status);
                        }
                    }) : resume(record.code).then(res=>{
                        if (checkResponse(res)) {
                            record.status = Number(!status);
                        }
                    });
                } else if (action == 'del') {
                    this.$confirm({
                        title: 'Are you sure you want to delete this account?',
                        content: 'Unrecoverable after deletion',
                        okText: 'delete',
                        okType: 'danger',
                        cancelText: 'quit',
                        onOk() {
                            del(record.code).then(() => {
                                app.init();
                            });
                            return Promise.resolve();
                        }
                    })
                }
            },
            handleSubmit() {
                let app = this;
                app.form.validateFields(
                    (err, values) => {
                        if (!err) {
                            app.handleOk();
                        }
                    })
            },
            handleOk() {
                let app = this;
                app.actionInfo.confirmLoading = true;
                let obj = app.form.getFieldsValue();
                if (app.newData.code) {
                    //edit
                    obj.code = app.newData.code;
                } else {
                    //Add
                    Object.assign(obj, app.newData);
                }
                doAccount(obj).then(res => {
                    app.actionInfo.confirmLoading = false;
                    if (!checkResponse(res)) {
                        return;
                    }
                    if (app.newData.code) {
                        app.newData.email = obj.email;
                        app.newData.name = obj.name;
                        app.newData.mobile = obj.mobile;
                        app.newData.description = obj.description;
                    } else {
                        app.dataSource.push(res.data);
                    }
                    app.form.resetFields();
                    app.newData = {id: 0};
                    app.actionInfo.modalStatus = false;

                });
            },
            auth(record, index) {
                let app = this;
                app.authInfo.modalStatus = true;
                app.currentUser = record;
                app.currentUser.index = index;
            },
            authApply() {
                let app = this;
                app.authInfo.confirmLoading = true;
                auth(app.currentUser.id, app.currentUser.authorize).then(res => {
                    if (checkResponse(res)) {
                        app.dataSource[app.currentUser.index] = app.currentUser;
                        app.authInfo.modalStatus = false;
                        app.authInfo.confirmLoading = false;
                    }
                })
            }
        }
    }
</script>
<style lang="less">
    .type-content{
        .content-item{
            margin-bottom: 12px;
            .ant-input{
                width: 90%;
            }
        }
    }
</style>
