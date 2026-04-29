import React, { useState, useEffect } from 'react';
import { Table, Tag, Space, Button, Modal, Form, Input, Select, message, Card, Statistic, Row, Col } from 'antd';
import { EyeOutlined, EditOutlined, DeleteOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import api from '../api';

const { Option } = Select;

const statusColors = {
  ONLINE: 'green',
  OFFLINE: 'orange',
  UPGRADING: 'blue',
  ERROR: 'red',
};

const statusNames = {
  ONLINE: '在线',
  OFFLINE: '离线',
  UPGRADING: '升级中',
  ERROR: '异常',
};

const Devices = () => {
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [currentDevice, setCurrentDevice] = useState(null);
  const [form] = Form.useForm();

  const fetchDevices = async () => {
    setLoading(true);
    try {
      const res = await api.get('/devices');
      setDevices(res.data);
    } catch (error) {
      message.error('获取设备列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDevices();
  }, []);

  const handleCreate = () => {
    setEditMode(false);
    setCurrentDevice(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record) => {
    setEditMode(true);
    setCurrentDevice(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = async (id) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这个设备吗？',
      onOk: async () => {
        try {
          await api.delete(`/devices/${id}`);
          message.success('删除成功');
          fetchDevices();
        } catch (error) {
          message.error('删除失败');
        }
      },
    });
  };

  const handleUpdateStatus = async (id, status) => {
    try {
      await api.put(`/devices/${id}/status?status=${status}`);
      message.success('状态更新成功');
      fetchDevices();
    } catch (error) {
      message.error('状态更新失败');
    }
  };

  const handleSubmit = async (values) => {
    try {
      if (editMode && currentDevice) {
        await api.put(`/devices/${currentDevice.id}`, values);
        message.success('更新成功');
      } else {
        await api.post('/devices', values);
        message.success('创建成功');
      }
      setModalVisible(false);
      fetchDevices();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const stats = {
    total: devices.length,
    online: devices.filter(d => d.status === 'ONLINE').length,
    offline: devices.filter(d => d.status === 'OFFLINE').length,
    error: devices.filter(d => d.status === 'ERROR').length,
  };

  const columns = [
    {
      title: '设备名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '设备型号',
      dataIndex: 'model',
      key: 'model',
    },
    {
      title: '区域',
      dataIndex: 'region',
      key: 'region',
    },
    {
      title: '当前版本',
      dataIndex: 'currentVersion',
      key: 'currentVersion',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => (
        <Tag color={statusColors[status]}>
          {statusNames[status]}
        </Tag>
      ),
    },
    {
      title: '失败次数',
      dataIndex: 'failCount',
      key: 'failCount',
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          {record.status !== 'ONLINE' && (
            <Button
              type="link"
              onClick={() => handleUpdateStatus(record.id, 'ONLINE')}
            >
              设为在线
            </Button>
          )}
          {record.status !== 'OFFLINE' && (
            <Button
              type="link"
              onClick={() => handleUpdateStatus(record.id, 'OFFLINE')}
            >
              设为离线
            </Button>
          )}
          <Button
            type="link"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record.id)}
          >
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic title="设备总数" value={stats.total} prefix={<EyeOutlined />} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="在线设备" value={stats.online} valueStyle={{ color: '#3f8600' }} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="离线设备" value={stats.offline} valueStyle={{ color: '#cf1322' }} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="异常设备" value={stats.error} valueStyle={{ color: '#faad14' }} />
          </Card>
        </Col>
      </Row>

      <div style={{ marginBottom: 16 }}>
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            添加设备
          </Button>
          <Button icon={<ReloadOutlined />} onClick={fetchDevices}>
            刷新
          </Button>
        </Space>
      </div>

      <Table
        columns={columns}
        dataSource={devices}
        rowKey="id"
        loading={loading}
      />

      <Modal
        title={editMode ? '编辑设备' : '添加设备'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Form.Item
            name="name"
            label="设备名称"
            rules={[{ required: true, message: '请输入设备名称' }]}
          >
            <Input placeholder="请输入设备名称" />
          </Form.Item>
          <Form.Item
            name="model"
            label="设备型号"
            rules={[{ required: true, message: '请输入设备型号' }]}
          >
            <Select placeholder="请选择设备型号">
              <Option value="ESP32-V1">ESP32-V1</Option>
              <Option value="ESP32-V2">ESP32-V2</Option>
              <Option value="STM32-L4">STM32-L4</Option>
              <Option value="STM32-F4">STM32-F4</Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="region"
            label="区域"
            rules={[{ required: true, message: '请选择区域' }]}
          >
            <Select placeholder="请选择区域">
              <Option value="CN-North">华北</Option>
              <Option value="CN-East">华东</Option>
              <Option value="CN-South">华南</Option>
              <Option value="US-East">美东</Option>
              <Option value="EU-West">西欧</Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="currentVersion"
            label="当前版本"
            rules={[{ required: true, message: '请输入当前版本' }]}
          >
            <Input placeholder="例如: 1.0.0" />
          </Form.Item>
          <Form.Item
            name="status"
            label="状态"
            rules={[{ required: true, message: '请选择状态' }]}
          >
            <Select placeholder="请选择状态">
              <Option value="ONLINE">在线</Option>
              <Option value="OFFLINE">离线</Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default Devices;
