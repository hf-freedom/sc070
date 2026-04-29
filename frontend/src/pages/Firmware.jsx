import React, { useState, useEffect } from 'react';
import { Table, Tag, Space, Button, Modal, Form, Input, Select, message, Descriptions } from 'antd';
import { PlusOutlined, EditOutlined, ReloadOutlined, EyeOutlined, CloudUploadOutlined } from '@ant-design/icons';
import api from '../api';

const { TextArea } = Input;

const statusColors = {
  DRAFT: 'default',
  PUBLISHED: 'green',
  DEPRECATED: 'orange',
};

const statusNames = {
  DRAFT: '草稿',
  PUBLISHED: '已发布',
  DEPRECATED: '已废弃',
};

const Firmware = () => {
  const [firmware, setFirmware] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [currentFirmware, setCurrentFirmware] = useState(null);
  const [form] = Form.useForm();

  const fetchFirmware = async () => {
    setLoading(true);
    try {
      const res = await api.get('/firmware');
      setFirmware(res.data);
    } catch (error) {
      message.error('获取固件列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFirmware();
  }, []);

  const handleCreate = () => {
    setEditMode(false);
    setCurrentFirmware(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record) => {
    setEditMode(true);
    setCurrentFirmware(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleView = (record) => {
    setCurrentFirmware(record);
    setDetailVisible(true);
  };

  const handlePublish = async (id) => {
    Modal.confirm({
      title: '发布固件',
      content: '确定要发布这个固件吗？发布后可用于升级计划。',
      onOk: async () => {
        try {
          await api.post(`/firmware/${id}/publish`);
          message.success('发布成功');
          fetchFirmware();
        } catch (error) {
          message.error('发布失败');
        }
      },
    });
  };

  const handleDeprecate = async (id) => {
    Modal.confirm({
      title: '废弃固件',
      content: '确定要废弃这个固件吗？',
      onOk: async () => {
        try {
          await api.post(`/firmware/${id}/deprecate`);
          message.success('已废弃');
          fetchFirmware();
        } catch (error) {
          message.error('操作失败');
        }
      },
    });
  };

  const handleSubmit = async (values) => {
    try {
      if (editMode && currentFirmware) {
        await api.put(`/firmware/${currentFirmware.id}`, values);
        message.success('更新成功');
      } else {
        await api.post('/firmware', values);
        message.success('创建成功');
      }
      setModalVisible(false);
      fetchFirmware();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const columns = [
    {
      title: '固件名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '版本号',
      dataIndex: 'version',
      key: 'version',
    },
    {
      title: '适用型号',
      dataIndex: 'supportedModels',
      key: 'supportedModels',
      render: (models) => (
        <Space>
          {models?.map(m => <Tag key={m}>{m}</Tag>)}
        </Space>
      ),
    },
    {
      title: '大小',
      dataIndex: 'size',
      key: 'size',
      render: (size) => size ? `${(size / 1024 / 1024).toFixed(2)} MB` : '-',
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
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="small">
          <Button type="link" icon={<EyeOutlined />} onClick={() => handleView(record)}>
            查看
          </Button>
          {record.status === 'DRAFT' && (
            <>
              <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
                编辑
              </Button>
              <Button type="link" onClick={() => handlePublish(record.id)}>
                发布
              </Button>
            </>
          )}
          {record.status === 'PUBLISHED' && (
            <Button type="link" danger onClick={() => handleDeprecate(record.id)}>
              废弃
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            添加固件
          </Button>
          <Button icon={<ReloadOutlined />} onClick={fetchFirmware}>
            刷新
          </Button>
        </Space>
      </div>

      <Table
        columns={columns}
        dataSource={firmware}
        rowKey="id"
        loading={loading}
      />

      <Modal
        title={editMode ? '编辑固件' : '添加固件'}
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
            label="固件名称"
            rules={[{ required: true, message: '请输入固件名称' }]}
          >
            <Input placeholder="请输入固件名称" />
          </Form.Item>
          <Form.Item
            name="version"
            label="版本号"
            rules={[{ required: true, message: '请输入版本号' }]}
          >
            <Input placeholder="例如: 1.0.0" />
          </Form.Item>
          <Form.Item
            name="supportedModels"
            label="适用型号"
            rules={[{ required: true, message: '请选择适用型号' }]}
          >
            <Select
              mode="multiple"
              placeholder="请选择适用型号"
            >
              <Select.Option value="ESP32-V1">ESP32-V1</Select.Option>
              <Select.Option value="ESP32-V2">ESP32-V2</Select.Option>
              <Select.Option value="STM32-L4">STM32-L4</Select.Option>
              <Select.Option value="STM32-F4">STM32-F4</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="checksum"
            label="校验码"
            rules={[{ required: true, message: '请输入校验码' }]}
          >
            <Input placeholder="SHA256校验码" />
          </Form.Item>
          <Form.Item
            name="size"
            label="文件大小(字节)"
          >
            <Input type="number" placeholder="文件大小" />
          </Form.Item>
          <Form.Item
            name="description"
            label="描述"
          >
            <TextArea rows={3} placeholder="固件描述" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="固件详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
      >
        {currentFirmware && (
          <Descriptions bordered column={1}>
            <Descriptions.Item label="固件名称">{currentFirmware.name}</Descriptions.Item>
            <Descriptions.Item label="版本号">{currentFirmware.version}</Descriptions.Item>
            <Descriptions.Item label="适用型号">
              {currentFirmware.supportedModels?.map(m => (
                <Tag key={m}>{m}</Tag>
              ))}
            </Descriptions.Item>
            <Descriptions.Item label="校验码">{currentFirmware.checksum}</Descriptions.Item>
            <Descriptions.Item label="文件大小">
              {currentFirmware.size ? `${(currentFirmware.size / 1024 / 1024).toFixed(2)} MB` : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={statusColors[currentFirmware.status]}>
                {statusNames[currentFirmware.status]}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="描述">{currentFirmware.description}</Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  );
};

export default Firmware;
