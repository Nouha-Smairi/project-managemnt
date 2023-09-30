<template>
    <div class="house-index">
        <wrapper-content>
           <!-- <div class="action">
                <a-button type="primary" icon="plus" @click="rowClick(null,'new')">Add to</a-button>
            </div>-->
            <a-table :columns="columns" :dataSource="dataSource" :loading="loading" rowKey="id" :pagination="pagination"
                     @change="pageChange">
                <template slot="action" slot-scope="text,record,index">
                    <a @click="rowClick(record,'edit')" v-if="record.owner_code == userInfo.code">edit</a>
                    <a @click="rowClick(record,'quit')" v-if="record.owner_code != userInfo.code">quit</a>
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
                        :form="form"
                        label='name of association'
                        :labelCol="{ span: 3 }"
                        :wrapperCol="{ span: 21 }"
                        fieldDecoratorId="name"
                        :fieldDecoratorOptions="{rules: [{ required: true, message: 'name of association' }]}"
                        help='Required, please fill in the organization name'
                >
                    <a-input placeholder='name of association'/>
                </a-form-item>
               <!-- <a-form-item
                        label='your region'
                        :labelCol="{ span: 3 }"
                        :wrapperCol="{ span: 21 }"
                        fieldDecoratorId="areas"
                        :fieldDecoratorOptions="{rules: [{ required: true, message: 'Please enter the area' }]}"
                        help='Required, please fill in the area.'
                >
                    <a-cascader :fieldNames="{ label: 'Name', value: 'ID', children: 'children' }" :options="options"
                                placeholder="Select the province and city where you are located"/>
                </a-form-item>-->

                <a-form-item
                        label='Address'
                        :labelCol="{ span: 3 }"
                        :wrapperCol="{span: 21 }"
                        fieldDecoratorId="address"
                        help="Address"
                >
                    <a-input/>
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
    </div>
</template>
<script>
    import {mapState} from 'vuex'
    import {list, doData, del} from '@/api/organization';
    import {checkResponse} from '@/assets/js/utils';
    import pagination from "@/mixins/pagination";
    import moment from 'moment';
    import {_quitOrganization} from "../../api/organization";

    const columns = [{
        title: 'name of association',
        dataIndex: 'name',
        width: '30%',
    }, {
        title: 'operate',
        scopedSlots: {
            customRender: 'action'
        }
    }];

    export default {
        mixins: [pagination],
        data() {
            return {
                columns,
                dataSource: [],
                loading: true,
                newData: {
                    code: 0
                },
                form: {},
                searchForm: {},
                actionInfo: {
                    modalStatus: false,
                    confirmLoading: false,
                    modalTitle: 'editorial organization',
                    okText: 'save',
                    cancelText: 'quit',
                },
                showMap: false,
                options: [],
            }
        },
        computed: {
            ...mapState({
                userInfo: state => state.userInfo,
            })
        },
        created() {
            this.init();
        },
        methods: {
            moment,
            init(quited) {
                let app = this;
                app.loading = true;
                list(app.requestData).then(res => {
                    app.dataSource = res.data.list;
                    app.pagination.total = res.data.total;
                    app.loading = false;
                    if (quited) {
                        //Re-update the current organization after exiting the organization
                        app.$store.dispatch('setCurrentOrganization',  res.data.list[0]);
                    }
                    app.$store.dispatch('setOrganizationList', res.data.list);
                });
                // areasData().then(res => {
                //     app.options = res.data;
                // });
            },
            rowClick(record, action) {
                let app = this;
                app.newData = {code: ''};
                if (action == 'add' || action == 'edit' || action == 'new') {
                    setTimeout(function () {
                        app.form && app.form.resetFields();
                    }, 0);
                    app.actionInfo.modalStatus = true;
                    app.actionInfo.modalTitle = 'add organization';
                    if (action == 'edit') {
                        //The modal sub-element will not be instantiated before the modal is displayed, and the processing will be delayed
                        setTimeout(function () {
                            app.actionInfo.modalTitle = 'editorial organization';
                            app.form.setFieldsValue({
                                name: record.name,
                                areas: [record.province.toString(), record.city.toString(), record.area.toString()],
                                address: record.address,
                            });
                            app.newData = record;
                        }, 0);
                    }
                }else if (action == 'quit') {
                    this.$confirm({
                        title: 'Are you sure you want to leave this organization?',
                        okText: 'quit',
                        okType: 'danger',
                        cancelText: 'Think again',
                        onOk() {
                            _quitOrganization({organizationCode: record.code}).then(res=>{
                                if (checkResponse(res)) {
                                    app.init(true);
                                }
                            });
                            return Promise.resolve();
                        }
                    });
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
                obj.areas = JSON.stringify(obj.areas);
                console.log(app.newData);
                if (app.newData.code) {
                    app.newData.organizationCode = app.newData.code
                    //edit
                    obj.organizationCode = app.newData.code;
                } else {
                    //Add
                    Object.assign(obj, app.newData);
                }
                doData(obj).then(res => {
                    app.actionInfo.confirmLoading = false;
                    if (!checkResponse(res)) {
                        return;
                    }
                    if (app.newData.code) {
                        app.newData.name = obj.name;
                        app.newData.address = obj.address;
                    } else {
                        app.dataSource.push(res.data);
                    }
                    app.form.resetFields();
                    app.newData = {code: 0};
                    app.actionInfo.modalStatus = false;
                    app.init();

                });
            }
        }
    }
</script>
