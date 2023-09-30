<template>
    <div class="project-auth">
        <wrapper-content>
            <div class="action">
                <Button type="primary" icon="plus" @click="rowClick(null,'new')">Add to</Button>
            </div>
            <Table :columns="columns" :dataSource="data" rowKey="id" :pagination="pagination" @change="pageChange">
                <template slot="is_default" slot-scope="text,record,index">
                    <a-icon type="check" v-if="record.is_default"/>
                    <!--<span v-else>disabled</span>-->
                </template>
                <template slot="action" slot-scope="text,record,index">

                    <a @click="rowClick(record,'edit')">edit</a>
                    <Divider type="vertical"/>
                    <router-link :to="'/system/account/apply/' + record.id">authorized</router-link>
                    <!--<a @click="rowClick(record,'status')"><span v-if="record.status">禁用</span><span v-else>enable</span></a>-->
                    <template v-if="!record.is_default">
                        <Divider type="vertical"/>
                        <a @click="rowClick(record,'is_default')"><span v-if="!record.is_default">set as Default</span></a>
                    </template>
                    <template v-if="record.canDelete">
                        <Divider type="vertical"/>
                        <a @click="rowClick(record,'del', index)">delete</a>
                    </template>
                </template>
            </Table>
        </wrapper-content>
        <Modal
                v-model="actionInfo.modalStatus"
                :title="actionInfo.modalTitle"
                :bodyStyle="{paddingBottom:'1px'}"
                :footer="null"
        >
            <Form
                    :autoFormCreate="(form)=>{this.form = form}"
                    @submit.prevent="handleSubmit"
            >
                <FormItem
                        :form="form"
                        label='name'
                        :labelCol="{ span: 3 }"
                        :wrapperCol="{ span: 21 }"
                        fieldDecoratorId="title"
                        :fieldDecoratorOptions="{rules: [{ required: true, message: 'please enter a name' }]}"
                        help='Required, please fill in the authority name (eg: finance), used to mark the role with the corresponding authority'
                >
                    <Input placeholder='permission name'/>
                </FormItem>
                <FormItem
                        label='describe'
                        :labelCol="{ span: 3 }"
                        :wrapperCol="{ span: 21 }"
                        fieldDecoratorId="desc"
                        help='Optional, permission description.'
                >
                    <Input placeholder='permission description'/>
                </FormItem>
                <FormItem
                        :wrapperCol="{ span: 21, offset: 3 }"
                >
                    <div class="action-btn">
                        <Button type="primary" htmlType='submit'
                                :loading="actionInfo.confirmLoading"
                                class="middle-btn">save
                        </Button>
                        <Button class="middle-btn" @click="actionInfo.modalStatus = false">Cancel
                        </Button>
                    </div>
                </FormItem>
            </Form>
        </Modal>
    </div>
</template>
<script>
    import {Table, Button, Divider, Modal, Form, Input} from 'ant-design-vue';
    import {getAuthList, doAuth, forbid, resume, del} from '@/api/auth';
    import {checkResponse} from '@/assets/js/utils';
    import pagination from "@/mixins/pagination";
    import {setDefault} from "../../../api/auth";

    const FormItem = Form.Item;

    const columns = [{
        title: 'name',
        dataIndex: 'title',
        width: '15%',
    }, {
        title: 'describe',
        dataIndex: 'desc',
        width: '30%',
    }, {
        title: 'default',
        dataIndex: 'is_default',
        width: '10%',
        scopedSlots: {
            customRender: 'is_default'
        },
    }, {
        title: 'add time',
        dataIndex: 'create_at',
    }, {
        title: 'operate',
        dataIndex: 'id',
        scopedSlots: {
            customRender: 'action'
        }
    }];
    export default {
        components: {
            Table,
            Button,
            Divider,
            Modal,
            Form,
            FormItem,
            Input,
        },
        mixins: [pagination],
        data() {
            return {
                columns,
                data: [],
                newData: null,
                form: {},
                actionInfo: {
                    modalStatus: false,
                    confirmLoading: false,
                    modalTitle: 'edit permissions',
                    okText: 'Sure',
                    cancelText: 'quit',
                },
                authApplyShow: false,
                nodeList: [],
                checkedList: [],
            }
        },
        created() {
            this.init();
        },
        methods: {
            init() {
                getAuthList(this.requestData).then(res => {
                    this.data = res.data.list;
                    this.pagination.total = res.data.total;
                });
            },
            rowClick(record, action, index) {
                let app = this;
                app.newData = null;
                if (action == 'edit' || action == 'new') {
                    setTimeout(function () {
                        app.form && app.form.resetFields();
                    }, 0);
                    app.actionInfo.modalStatus = true;
                    app.actionInfo.modalTitle = 'Add permissions';
                    if (action == 'edit') {
                        //The modal sub-element will not be instantiated before the modal is displayed, and the processing will be delayed
                        setTimeout(function () {
                            app.actionInfo.modalTitle = 'edit permissions';
                            app.form.setFieldsValue({
                                title: record.title,
                                desc: record.desc,
                            });
                            app.newData = record;
                        }, 0);
                    } else {
                        app.newData = {
                            status: 1,
                            sort: 0,
                        };
                    }
                } else if (action == 'status') {
                    const status = record.status;
                    record.status = Number(!status);
                    status ? forbid(record.id, record.status) : resume(record.id, record.status);
                } else if (action == 'is_default') {
                    setDefault(record.id,1).then(res=>{
                        if (checkResponse(res)) {
                            app.init();
                        }
                    });
                } else if (action == 'del') {
                    Modal.confirm({
                        title: 'Are you sure you want to remove this permission?',
                        content: 'Unrecoverable after deletion',
                        okText: 'Sure',
                        okType: 'danger',
                        cancelText: 'quit',
                        onOk() {
                            del(record.id).then(res => {
                                if (checkResponse(res)) {
                                    app.data.splice(index, 1);
                                }
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
                if (app.newData.id) {
                    //edit
                    obj.id = app.newData.id;
                } else {
                    //Add
                    Object.assign(obj, app.newData);
                }
                console.log(obj);
                doAuth(obj).then(res => {
                    app.actionInfo.confirmLoading = false;
                    if (!checkResponse(res)) {
                        return;
                    }
                    if (app.newData.id) {
                        app.newData.title = obj.title;
                        app.newData.desc = obj.desc;
                    } else {
                        app.data.push(res.data);
                    }
                    app.form.resetFields();
                    app.newData = null;
                    app.actionInfo.modalStatus = false;
                });
            },
        }
    }
</script>
