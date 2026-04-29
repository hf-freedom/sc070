import React, { useState, useEffect } from 'react';
import { Table, Tag, Space, Button, Modal, Form, Input, Select, message, Descriptions, Card } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined, EyeOutlined } from '@ant-design/icons';
import api from '../api';

const Groups = () => {
  const [groups, setGroups] = useState([]);
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [currentGroup, setCurrentGroup] = useState(null);
  const [form] = Form.useForm();

  const fetchData = async () => {
    setLoading(true);
    try {
      const [groupsRes, devicesRes] = await Promise.all([
        api.get('/groups'),
        api.get('/devices'),
      ]);
      setGroups(groupsRes.data);
      setDevices(devicesRes.data);
    } catch (error) {
      message.error('获取数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const getDeviceName = (id) => {
    const device = devices.find(d => d.id === id);
    return device ? device.name : id;
  };

  const handleCreate = () => {
    setEditMode(false);
    setCurrentGroup(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record) => {
    setEditMode(true);
    setCurrentGroup(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleView = (record) => {
    setCurrentGroup(record);
    setDetailVisible(true);
  };

  const handleDelete = async (id) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这个设备组吗？',
      onOk: async () => {
        try {
          await api.delete(`/groups/${id}`);
          message.success('删除成功');
          fetchData();
        } catch (error) {
          message.error('删除失败');
        }
      },
    });
  };

  const handleSubmit = async (values) => {
    try {
      if (editMode && currentGroup) {
        await api.put(`/groups/${currentGroup.id}`, values);
        message.success('更新成功');
      } else {
        await api.post('/groups', values);
        message.success('创建成功');
      }
      setModalVisible(false);
      fetchData();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const columns = [
    {
      title: '分组名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: '设备数量',
      key: 'deviceCount',
      render: (_, record) => record.deviceIds?.length || 0,
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="small">
          <Button type="link" icon={<EyeOutlined />} onClick={() => handleView(record)}>
            查看
          </Button>
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            添加分组
          </Button>
          <Button icon={<ReloadOutlined />} onClick={fetchData}>
            刷新
          </Button>
        </Space>
      </div>

      <Table
        columns={columns}
        dataSource={groups}
        rowKey="id"
        loading={loading}
      />

      <Modal
        title={editMode ? '编辑分组' : '添加分组'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Form.Item
            name="name"
            label="分组名称"
            rules={[{ required: true, message: '请输入分组名称' }]}
          >
            <Input placeholder="请输入分组名称" />
          </Form.Item>
          <Form.Item
            name="description"
            label="描述"
          >
            <Input.TextArea rows={3} placeholder="请输入分组描述" />
          </Form.Item>
          <Form.Item
            name="deviceIds"
            label="选择设备"
          >
            <Select
              mode="multiple"
              placeholder="请选择设备"
              optionFilterProp="children"
            >
              {devices.map(device => (
                <Select.Option key={device.id} value={device.id}>
                  {device.name} ({device.model})
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="分组详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
      >
        {currentGroup && (
          <div>
            <Descriptions bordered column={1}>
              <Descriptions.Item label="分组名称">{currentGroup.name}</Descriptions.Item>
              <Descriptions.Item label="描述">{currentGroup.description}</Descriptions.Item>
              <Descriptions.Item label="设备数量">{currentGroup.deviceIds?.length || 0}</Descriptions.Item>
            </Descriptions>
            {currentGroup.deviceIds && currentGroup.deviceIds.length > 0 && (
              <div style={{ marginTop: 16 }}>
                <h4>设备列表</h4>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                  {currentGroup.deviceIds.map(id => (
                    <Tag key={id} color="blue">{getDeviceName(id)}</Tag>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};

export default Groups;
