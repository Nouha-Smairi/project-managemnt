<template>
  <div class="admin-menu">
    <wrapper-content>
      <div class="action">
        <Button type="primary" icon="plus" @click="rowClick(null,'new')">Add to</Button>
      </div>
      <Table :columns="columns" :dataSource="dataSource" :loading="loading" :pagination="false">
        <template slot="url" slot-scope="text,record,index">
          <span v-if="record.params">{{record.url + '/' + record.params}}</span>
          <span v-else>{{record.url}}</span>
          <span v-if="record.values !== null && record.values !== ''"> - {{record.values}}</span>
        </template>
        <template slot="status" slot-scope="text,record,index">
          <span class="success-color" v-if="record.status">Using</span>
          <span v-else>disabled</span>
        </template>
        <template slot="action" slot-scope="text,record,index">
          <a :disabled="record.dept == 3" @click="rowClick(record,'add')">Add subordinate</a>
          <Divider type="vertical"/>
          <a @click="rowClick(record,'edit')">edit</a>
          <Divider type="vertical"/>
          <a @click="rowClick(record,'status')"><span v-if="record.status">disabled</span><span v-else>enable</span></a>
          <Divider type="vertical"/>
          <a @click="rowClick(record,'del')">delete</a>
        </template>
      </Table>
    </wrapper-content>
    <Modal
        v-model="actionInfo.modalStatus"
        :width="800"
        :title="actionInfo.modalTitle"
        :bodyStyle="{paddingBottom:'1px'}"
        :footer="null"
    >
      <Form
          :autoFormCreate="(form)=>{this.form = form}"
          @submit.prevent="handleSubmit"
      >
        <a-row :gutter="24">
          <a-col :span="12">
            <FormItem
                :form="form"
                label='name'
                :labelCol="{ span: 3 }"
                :wrapperCol="{ span: 21 }"
                fieldDecoratorId="title"
                :fieldDecoratorOptions="{rules: [{ required: true, message: 'please enter a name' }]}"
                help='Required, please fill in the menu name (eg: system management)，It is recommended that the characters should not be too long, generally 4-6 Chinese characters'
            >
              <Input placeholder='menu name'/>
            </FormItem>
          </a-col>
          <a-col :span="12">
            <FormItem
                label='address'
                :labelCol="{ span: 3 }"
                :wrapperCol="{ span: 21 }"
                fieldDecoratorId="url"
                :fieldDecoratorOptions="{rules: [{ required: true, message: 'Please enter the routing address' }]}"
                help='Required, please fill in the routing address（如：admin/user/index，The corresponding files are located at：views/admin/user/index.vue）. If it is the parent menu, please fill in the "#" symbol, do not fill in the routing address.'
            >
              <Input placeholder='routing address'/>
            </FormItem>
          </a-col>
          <a-col :span="12">
            <FormItem
                label='path'
                :labelCol="{ span: 3 }"
                :wrapperCol="{ span: 21 }"
                fieldDecoratorId="file_path"
                help='Optional, file path. The default is the same as the address, and special file paths can be defined here'
            >
              <Input placeholder='routing address'/>
            </FormItem>
          </a-col>
          <a-col :span="12">
            <a-form-item
                label='node'
                :labelCol="{ span: 3 }"
                :wrapperCol="{ span: 21 }"
                fieldDecoratorId="node"
                :fieldDecoratorOptions="{rules: [{ required: true, message: 'Please bind the authority node' }]}"
                help='Mandatory, bind the current route to the permission node, and the menu without the permission of the node will be hidden. Do not bind, please fill in the "#" symbol.'
            >

              <a-auto-complete  placeholder="Please select a permission node" @search="fetchNode" >
                <template slot="dataSource">
                  <a-select-option v-for="node in nodeList" :key="node.node">{{node.node}} - {{node.title}}
                  </a-select-option>
                </template>
                <a-input>
                  <a-icon slot="suffix" type="search" class="certain-category-icon" />
                </a-input>

              </a-auto-complete>

<!--              <a-select-->
<!--                  showSearch-->
<!--                  placeholder="Please select a permission node"-->
<!--                  :filterOption="false"-->
<!--                  @search="fetchNode"-->
<!--                  @change="handleChange"-->
<!--                  :notFoundContent="fetching ? undefined : null"-->
<!--              >-->
<!--                <a-spin v-if="fetching" slot="notFoundContent" size="small"/>-->
<!--                <a-select-option v-for="node in nodeList" :key="node.node">{{node.node}} - {{node.title}}-->
<!--                </a-select-option>-->
<!--              </a-select>-->
              <!-- <a-tree-select
                       showSearch
                       treeNodeFilterProp="title"
                       :showCheckedStrategy="SHOW_ALL"
                       treeNodeLabelProp="node"
                       :dropdownStyle="{ maxHeight: '400px', overflow: 'auto' }"
                       :treeData="nodeList"
                       placeholder='Please select a permission node'
                       treeDefaultExpandAll
               >
               </a-tree-select>-->
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <FormItem
                label='to sort'
                :labelCol="{ span: 3 }"
                :wrapperCol="{ span: 21 }"
                fieldDecoratorId="sort"
                :fieldDecoratorOptions="{rules: [{ required: true, message: 'Please enter sort' }],initialValue:0}"
                help='Required, an integer. The smaller the number, the higher the front, and the minimum value is 0.'
            >
              <Input placeholder='to sort'/>
            </FormItem>
          </a-col>
          <a-col :span="12">
            <FormItem
                label='parameter'
                :labelCol="{ span: 3 }"
                :wrapperCol="{ span: 21 }"
                fieldDecoratorId="params"
                help='Optional, please fill in the additional parameters of the route (eg: :id, the route is: admin/user/index/:id)'
            >
              <Input placeholder='extra parameters'/>
            </FormItem>
          </a-col>
          <a-col :span="12">
            <FormItem
                label='value'
                :labelCol="{ span: 3 }"
                :wrapperCol="{ span: 21 }"
                fieldDecoratorId="values"
                help='Optional, please fill in the additional parameter value of the route (for example, if the route is admin/user/index/:id, and the parameter value is 1, the final link will be resolved to: admin/user/index/1)'
            >
              <Input placeholder='parameter value'/>
            </FormItem>
          </a-col>
          <a-col :span="12">
            <FormItem
                label='icon'
                :labelCol="{ span: 3 }"
                :wrapperCol="{ span: 21 }"
                fieldDecoratorId="icon"
                help='Optional, set menu option front icon'
            >
              <Input placeholder='front icon'/>
            </FormItem>
          </a-col>
        </a-row>
        <a-row :gutter="24">
          <a-col :span="12">
            <FormItem
                label='Sidebar'
                :labelCol="{ span: 3 }"
                :wrapperCol="{ span: 21 }"
                fieldDecoratorId="show_slider"
                :fieldDecoratorOptions="{valuePropName:'checked',initialValue: true}"
                help='Optional, if unchecked, the page will not display the side menu bar'
            >
              <Checkbox>Whether to show the sidebar</Checkbox>
            </FormItem>
          </a-col>
          <a-col :span="12">
            <FormItem
                label='inner page'
                :labelCol="{ span: 3 }"
                :wrapperCol="{ span: 21 }"
                fieldDecoratorId="is_inner"
                :fieldDecoratorOptions="{valuePropName:'checked',initialValue: false}"
                help='Optional, if selected as the inner page of the menu, it will not be displayed on the navigation menu. Used for menu selection item rendering judgment'
            >
              <Checkbox>Is it an inner page</Checkbox>
            </FormItem>
          </a-col>
        </a-row>
        <FormItem
        >
          <div class="action-btn" style="text-align: center">
            <Button type="primary" htmlType='submit'
                    size="large"
                    :loading="actionInfo.confirmLoading"
                    class="middle-btn">save
            </Button>
            <Button class="middle-btn" size="large" @click="actionInfo.modalStatus = false">Cancel
            </Button>
          </div>
        </FormItem>
      </Form>
    </Modal>
  </div>
</template>
<script>
import {Table, Button, Divider, Modal, Form, Input, Checkbox} from 'ant-design-vue';
import {getMenu, doMenu, forbid, resume, delMenu} from '@/api/menu';
import {createRoute, checkResponse} from '@/assets/js/utils';
import {getStore} from '@/assets/js/storage';
import {allList} from "@/api/node";
import debounce from 'lodash/debounce';

const FormItem = Form.Item;

const columns = [{
  title: 'name',
  dataIndex: 'title',
  width: '15%',
}, {
  title: 'address',
  dataIndex: 'url',
  width: '20%',
  scopedSlots: {
    customRender: 'url'
  },
}, {
  title: 'path',
  dataIndex: 'file_path',
}, {
  title: 'node',
  width: '20%',
  dataIndex: 'node',
}, {
  title: 'to sort',
  dataIndex: 'sort',
}, {
  title: 'type',
  dataIndex: 'innerText',
},
  //     {
  //     title: 'state',
  //     dataIndex: 'statusText',
  //     width: '15%',
  //     scopedSlots: {
  //         customRender: 'status'
  //     },
  // },
  {
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
    Checkbox
  },
  data() {
    this.lastFetchId = 0;
    this.fetchNode = debounce(this.fetchNode, 800);
    return {
      columns,
      data: [],
      dataSource: [],
      nodeList: [],
      loading: true,
      fetching: false,
      newMenu: null,
      form: {},
      actionInfo: {
        modalStatus: false,
        confirmLoading: false,
        modalTitle: 'edit menu',
        okText: 'Sure',
        cancelText: 'quit',
      }
    }
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      let app = this;
      app.loading = true;
      getMenu().then(res => {
        app.loading = false;
        let list = res.data;
        list.forEach(function (v) {
          v.key = v.id;
          v.dept = 1;
          if (v.children) {
            v.children.forEach(function (v2) {
              v2.key = v2.id;
              v2.dept = 2;
              if (v2.children) {
                v2.children.forEach(function (v3) {
                  v3.key = v3.id;
                  v3.dept = 3;
                })
              }
            })
          }
        });
        app.dataSource = list;
      });
    },
    rowClick(record, action) {
      let app = this;
      app.newMenu = null;
      app.nodeList = [];
      if (action == 'add' || action == 'edit' || action == 'new') {
        setTimeout(function () {
          app.form && app.form.resetFields();
        }, 0);
        app.actionInfo.modalStatus = true;
        app.actionInfo.modalTitle = 'add menu';
        if (action == 'edit') {
          //The modal sub-element will not be instantiated before the modal is displayed, and the processing will be delayed
          setTimeout(function () {
            app.actionInfo.modalTitle = 'edit menu';
            app.form.setFieldsValue({
              title: record.title,
              file_path: record.file_path,
              url: record.url,
              sort: record.sort,
              node: record.node,
              params: record.params,
              values: record.values,
              icon: record.icon,
              is_inner: record.is_inner,
              show_slider: record.show_slider,
            });
            app.newMenu = record;
          }, 0);
        } else if (action == 'add') {
          app.newMenu = {
            status: 1,
            dept: parseInt(record.dept) + 1
          };
          app.newMenu.pid = record.id;
        } else {
          app.newMenu = {
            status: 1,
            dept: 1,
            pid: 0
          };
        }
      } else if (action == 'status') {
        const status = record.status;
        record.status = Number(!status);
        status ? forbid(record.id, record.status) : resume(record.id, record.status);
      } else if (action == 'del') {
        const confirm = Modal.confirm({
          title: 'Are you sure you want to delete this menu?',
          content: 'It cannot be restored after deletion, and the submenu will be deleted at the same time',
          okText: 'Sure',
          okType: 'danger',
          cancelText: 'quit',
          onOk() {
            delMenu(record.id).then(res => {
              app.dataSource.forEach(function (v, k) {
                if (v.id == record.id) {
                  app.dataSource.splice(k, 1);
                  return;
                }
                if (v.children) {
                  v.children.forEach(function (v2, k2) {
                    if (v2.id == record.id) {
                      v.children.splice(k2, 1);
                      return;
                    }
                    if (v2.children) {
                      v2.children.forEach(function (v3, k3) {
                        if (v3.id == record.id) {
                          v2.children.splice(k3, 1);
                          return;
                        }
                      })
                    }
                  });
                }
              });
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
      if (app.newMenu.id) {
        //edit
        obj.id = app.newMenu.id;
      } else {
        //Add
        Object.assign(obj, app.newMenu);
      }
      doMenu(obj).then(res => {
        app.actionInfo.confirmLoading = false;
        if (!checkResponse(res)) {
          return;
        }
        // app.freshMenu();
        if (app.newMenu.id) {
          app.newMenu.title = obj.title;
          app.newMenu.url = obj.url;
          app.newMenu.sort = obj.sort;
          app.newMenu.file_path = obj.file_path;
          app.newMenu.node = obj.node;
          app.newMenu.params = obj.params;
          app.newMenu.values = obj.values;
          app.newMenu.icon = obj.icon;
          app.newMenu.is_inner = !!obj.is_inner;
          app.newMenu.show_slider = !!obj.show_slider;
          app.dataSource.forEach(function (v) {
            if (v.id == obj.id) {
              Object.assign(v, obj);
              return;
            }
            if (v.children) {
              v.children.forEach(function (v2) {
                if (v2.id == obj.id) {
                  Object.assign(v2, obj);
                  return;
                }
                if (v2.children) {
                  v2.children.forEach(function (v3) {
                    if (v3.id == obj.id) {
                      Object.assign(v3, obj);
                      return;
                    }
                  })
                }
              });
            }
          });
        } else {
          obj = res.data;
          obj.key = res.data.id;
          obj.is_inner = !!res.data.is_inner;
          obj.show_slider = !!res.data.show_slider;
          obj.dept = app.newMenu.dept;
          if (app.newMenu.pid == 0) {
            app.dataSource.push(obj);
          } else {
            app.dataSource.forEach(function (v) {
              if (v.id == obj.pid) {
                if (v.children) {
                  v.children.push(obj);
                } else {
                  v.children = [obj];
                }
                return;
              }
              if (v.children) {
                v.children.forEach(function (v2) {
                  if (v2.id == obj.pid) {
                    if (v2.children) {
                      v2.children.push(obj);
                    } else {
                      v2.children = [obj];
                    }
                    return;
                  }
                  if (v2.children) {
                    v2.children.forEach(function (v3) {
                      if (v3.id == obj.id) {
                        if (v3.children) {
                          v3.children.push(obj);
                        } else {
                          v3.children = [obj];
                        }
                        return;
                      }
                    })
                  }
                });
              }
            });
          }
        }
        app.form.resetFields();
        app.newMenu = null;
        app.actionInfo.modalStatus = false;
      });
    },
    handleChange(value) {
      this.nodeList = [];
      this.fetching = false;
      // Object.assign(this, {
      //     data: [],
      //     fetching: false,
      // })
    },
    fetchNode(value) {
      let app = this;
      this.lastFetchId += 1;
      const fetchId = this.lastFetchId;
      this.nodeList = [];
      this.fetching = true;
      allList('project',value).then(res => {
        if (fetchId !== app.lastFetchId) { // for fetch callback order
          return;
        }
        app.fetching = false;
        app.nodeList = res.data;
      });
    },
  }
}
</script>
