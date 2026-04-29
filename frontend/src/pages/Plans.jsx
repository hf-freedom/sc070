import React, { useState, useEffect } from 'react';
import { Table, Tag, Space, Button, Modal, Form, Input, Select, message, Descriptions, InputNumber, Statistic, Row, Col, Card } from 'antd';
import { PlusOutlined, EditOutlined, ReloadOutlined, EyeOutlined, PlayCircleOutlined, PauseCircleOutlined, StopOutlined } from '@ant-design/icons';
import api from '../api';

const statusColors = {
  DRAFT: 'default',
  RUNNING: 'processing',
  PAUSED: 'warning',
  COMPLETED: 'success',
  CANCELLED: 'error',
};

const statusNames = {
  DRAFT: '草稿',
  RUNNING: '运行中',
  PAUSED: '已暂停',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
};

const Plans = () => {
  const [plans, setPlans] = useState([]);
  const [firmware, setFirmware] = useState([]);
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [currentPlan, setCurrentPlan] = useState(null);
  const [form] = Form.useForm();

  const fetchData = async () => {
    setLoading(true);
    try {
      const [plansRes, firmwareRes, groupsRes] = await Promise.all([
        api.get('/plans'),
        api.get('/firmware'),
        api.get('/groups'),
      ]);
      setPlans(plansRes.data);
      setFirmware(firmwareRes.data.filter(f => f.status === 'PUBLISHED'));
      setGroups(groupsRes.data);
    } catch (error) {
      message.error('获取数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const getFirmwareName = (id) => {
    const f = firmware.find(f => f.id === id);
    return f ? `${f.name} v${f.version}` : id;
  };

  const getGroupName = (id) => {
    const g = groups.find(g => g.id === id);
    return g ? g.name : id;
  };

  const handleCreate = () => {
    setEditMode(false);
    setCurrentPlan(null);
    form.resetFields();
    form.setFieldsValue({
      grayPercentage: 100,
      successRateThreshold: 0.8,
    });
    setModalVisible(true);
  };

  const handleEdit = (record) => {
    if (record.status !== 'DRAFT') {
      message.warning('只能编辑草稿状态的计划');
      return;
    }
    setEditMode(true);
    setCurrentPlan(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleView = (record) => {
    setCurrentPlan(record);
    setDetailVisible(true);
  };

  const handleStart = async (id) => {
    Modal.confirm({
      title: '启动计划',
      content: '确定要启动这个升级计划吗？',
      onOk: async () => {
        try {
          await api.post(`/plans/${id}/start`);
          message.success('计划已启动');
          fetchData();
        } catch (error) {
          message.error('启动失败');
        }
      },
    });
  };

  const handlePause = async (id) => {
    Modal.confirm({
      title: '暂停计划',
      content: '确定要暂停这个升级计划吗？',
      onOk: async () => {
        try {
          await api.post(`/plans/${id}/pause`);
          message.success('计划已暂停');
          fetchData();
        } catch (error) {
          message.error('暂停失败');
        }
      },
    });
  };

  const handleCancel = async (id) => {
    Modal.confirm({
      title: '取消计划',
      content: '确定要取消这个升级计划吗？此操作不可恢复。',
      onOk: async () => {
        try {
          await api.post(`/plans/${id}/cancel`);
          message.success('计划已取消');
          fetchData();
        } catch (error) {
          message.error('取消失败');
        }
      },
    });
  };

  const handleSubmit = async (values) => {
    try {
      if (editMode && currentPlan) {
        await api.put(`/plans/${currentPlan.id}`, values);
        message.success('更新成功');
      } else {
        await api.post('/plans', values);
        message.success('创建成功');
      }
      setModalVisible(false);
      fetchData();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const stats = {
    total: plans.length,
    running: plans.filter(p => p.status === 'RUNNING').length,
    completed: plans.filter(p => p.status === 'COMPLETED').length,
  };

  const columns = [
    {
      title: '计划名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '目标固件',
      dataIndex: 'firmwareId',
      key: 'firmwareId',
      render: (id) => getFirmwareName(id),
    },
    {
      title: '灰度比例',
      dataIndex: 'grayPercentage',
      key: 'grayPercentage',
      render: (val) => `${val}%`,
    },
    {
      title: '批次进度',
      key: 'batch',
      render: (_, record) => `${record.currentBatch || 0}/${record.totalBatches || 0}`,
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
              <Button type="link" icon={<PlayCircleOutlined />} onClick={() => handleStart(record.id)}>
                启动
              </Button>
            </>
          )}
          {record.status === 'RUNNING' && (
            <Button type="link" icon={<PauseCircleOutlined />} onClick={() => handlePause(record.id)}>
              暂停
            </Button>
          )}
          {record.status === 'PAUSED' && (
            <Button type="link" icon={<PlayCircleOutlined />} onClick={() => handleStart(record.id)}>
              继续
            </Button>
          )}
          {(record.status === 'DRAFT' || record.status === 'RUNNING' || record.status === 'PAUSED') && (
            <Button type="link" danger icon={<StopOutlined />} onClick={() => handleCancel(record.id)}>
              取消
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Card>
            <Statistic title="计划总数" value={stats.total} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic title="运行中" value={stats.running} valueStyle={{ color: '#1890ff' }} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic title="已完成" value={stats.completed} valueStyle={{ color: '#3f8600' }} />
          </Card>
        </Col>
      </Row>

      <div style={{ marginBottom: 16 }}>
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            创建计划
          </Button>
          <Button icon={<ReloadOutlined />} onClick={fetchData}>
            刷新
          </Button>
        </Space>
      </div>

      <Table
        columns={columns}
        dataSource={plans}
        rowKey="id"
        loading={loading}
      />

      <Modal
        title={editMode ? '编辑计划' : '创建计划'}
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
            label="计划名称"
            rules={[{ required: true, message: '请输入计划名称' }]}
          >
            <Input placeholder="请输入计划名称" />
          </Form.Item>
          <Form.Item
            name="firmwareId"
            label="目标固件"
            rules={[{ required: true, message: '请选择目标固件' }]}
          >
            <Select placeholder="请选择目标固件">
              {firmware.map(f => (
                <Select.Option key={f.id} value={f.id}>
                  {f.name} v{f.version}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="groupIds"
            label="目标分组"
            rules={[{ required: true, message: '请选择目标分组' }]}
          >
            <Select
              mode="multiple"
              placeholder="请选择目标分组"
            >
              {groups.map(g => (
                <Select.Option key={g.id} value={g.id}>
                  {g.name}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="grayPercentage"
            label="灰度比例(%)"
            rules={[{ required: true, message: '请输入灰度比例' }]}
          >
            <InputNumber min={1} max={100} style={{ width: '100%' }} placeholder="1-100" />
          </Form.Item>
          <Form.Item
            name="successRateThreshold"
            label="成功率阈值"
            rules={[{ required: true, message: '请输入成功率阈值' }]}
          >
            <InputNumber min={0.1} max={1} step={0.1} style={{ width: '100%' }} placeholder="0.1-1.0" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="计划详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
      >
        {currentPlan && (
          <Descriptions bordered column={1}>
            <Descriptions.Item label="计划名称">{currentPlan.name}</Descriptions.Item>
            <Descriptions.Item label="目标固件">{getFirmwareName(currentPlan.firmwareId)}</Descriptions.Item>
            <Descriptions.Item label="目标分组">
              {currentPlan.groupIds?.map(id => (
                <Tag key={id}>{getGroupName(id)}</Tag>
              ))}
            </Descriptions.Item>
            <Descriptions.Item label="灰度比例">{currentPlan.grayPercentage}%</Descriptions.Item>
            <Descriptions.Item label="批次进度">{currentPlan.currentBatch || 0} / {currentPlan.totalBatches || 0}</Descriptions.Item>
            <Descriptions.Item label="成功率阈值">{(currentPlan.successRateThreshold * 100).toFixed(0)}%</Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={statusColors[currentPlan.status]}>
                {statusNames[currentPlan.status]}
              </Tag>
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  );
};

export default Plans;
