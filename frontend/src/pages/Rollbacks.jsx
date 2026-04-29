import React, { useState, useEffect } from 'react';
import { Table, Tag, Space, Button, Modal, Form, Input, Select, message, Descriptions, Card, Statistic, Row, Col } from 'antd';
import { PlusOutlined, ReloadOutlined, EyeOutlined, PlayCircleOutlined } from '@ant-design/icons';
import api from '../api';

const statusColors = {
  PENDING: 'default',
  IN_PROGRESS: 'processing',
  SUCCESS: 'success',
  FAILED: 'error',
};

const statusNames = {
  PENDING: '待处理',
  IN_PROGRESS: '进行中',
  SUCCESS: '成功',
  FAILED: '失败',
};

const Rollbacks = () => {
  const [rollbacks, setRollbacks] = useState([]);
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [currentRollback, setCurrentRollback] = useState(null);
  const [form] = Form.useForm();

  const fetchData = async () => {
    setLoading(true);
    try {
      const [rollbacksRes, devicesRes] = await Promise.all([
        api.get('/rollback'),
        api.get('/devices'),
      ]);
      setRollbacks(rollbacksRes.data);
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
    const d = devices.find(d => d.id === id);
    return d ? `${d.name} (${d.currentVersion})` : id;
  };

  const handleCreate = () => {
    form.resetFields();
    setModalVisible(true);
  };

  const handleView = (record) => {
    setCurrentRollback(record);
    setDetailVisible(true);
  };

  const handleExecute = async (id) => {
    Modal.confirm({
      title: '执行回滚',
      content: '确定要执行这个回滚任务吗？',
      onOk: async () => {
        try {
          await api.post(`/rollback/${id}/execute`);
          message.success('回滚已开始');
          fetchData();
        } catch (error) {
          message.error('执行失败');
        }
      },
    });
  };

  const handleSubmit = async (values) => {
    try {
      await api.post(`/rollback?deviceId=${values.deviceId}&reason=${encodeURIComponent(values.reason)}`);
      message.success('创建成功');
      setModalVisible(false);
      fetchData();
    } catch (error) {
      message.error('操作失败，请检查该设备是否有可用的旧版本');
    }
  };

  const stats = {
    total: rollbacks.length,
    success: rollbacks.filter(r => r.status === 'SUCCESS').length,
    failed: rollbacks.filter(r => r.status === 'FAILED').length,
  };

  const columns = [
    {
      title: '设备',
      dataIndex: 'deviceId',
      key: 'deviceId',
      render: (id) => getDeviceName(id),
    },
    {
      title: '从版本',
      dataIndex: 'fromVersion',
      key: 'fromVersion',
    },
    {
      title: '回滚到',
      dataIndex: 'toVersion',
      key: 'toVersion',
    },
    {
      title: '原因',
      dataIndex: 'reason',
      key: 'reason',
      ellipsis: true,
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
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="small">
          <Button type="link" icon={<EyeOutlined />} onClick={() => handleView(record)}>
            详情
          </Button>
          {record.status === 'PENDING' && (
            <Button type="link" icon={<PlayCircleOutlined />} onClick={() => handleExecute(record.id)}>
              执行
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
            <Statistic title="回滚总数" value={stats.total} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic title="成功" value={stats.success} valueStyle={{ color: '#3f8600' }} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic title="失败" value={stats.failed} valueStyle={{ color: '#cf1322' }} />
          </Card>
        </Col>
      </Row>

      <div style={{ marginBottom: 16 }}>
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            创建回滚
          </Button>
          <Button icon={<ReloadOutlined />} onClick={fetchData}>
            刷新
          </Button>
        </Space>
      </div>

      <Table
        columns={columns}
        dataSource={rollbacks}
        rowKey="id"
        loading={loading}
      />

      <Modal
        title="创建回滚任务"
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
            name="deviceId"
            label="选择设备"
            rules={[{ required: true, message: '请选择设备' }]}
          >
            <Select placeholder="请选择设备（会自动查找可用的旧版本）">
              {devices.map(d => (
                <Select.Option key={d.id} value={d.id}>
                  {d.name} (当前版本: {d.currentVersion})
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="reason"
            label="回滚原因"
            rules={[{ required: true, message: '请输入回滚原因' }]}
          >
            <Input.TextArea rows={3} placeholder="请输入回滚原因" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="回滚详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
      >
        {currentRollback && (
          <Descriptions bordered column={1}>
            <Descriptions.Item label="设备">{getDeviceName(currentRollback.deviceId)}</Descriptions.Item>
            <Descriptions.Item label="从版本">{currentRollback.fromVersion}</Descriptions.Item>
            <Descriptions.Item label="回滚到">{currentRollback.toVersion}</Descriptions.Item>
            <Descriptions.Item label="原因">{currentRollback.reason}</Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={statusColors[currentRollback.status]}>
                {statusNames[currentRollback.status]}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="创建时间">{currentRollback.createTime}</Descriptions.Item>
            <Descriptions.Item label="完成时间">{currentRollback.completeTime || '-'}</Descriptions.Item>
            {currentRollback.errorMessage && (
              <Descriptions.Item label="错误信息">{currentRollback.errorMessage}</Descriptions.Item>
            )}
          </Descriptions>
        )}
      </Modal>
    </div>
  );
};

export default Rollbacks;
