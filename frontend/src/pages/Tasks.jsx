import React, { useState, useEffect } from 'react';
import { Table, Tag, Space, Button, Modal, message, Descriptions } from 'antd';
import { ReloadOutlined, EyeOutlined, InfoCircleOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import api from '../api';

const statusColors = {
  PENDING: 'default',
  QUEUED: 'warning',
  DELIVERED: 'processing',
  DOWNLOADING: 'processing',
  INSTALLING: 'processing',
  REBOOTING: 'processing',
  SUCCESS: 'success',
  FAILED: 'error',
  CANCELLED: 'default',
};

const statusNames = {
  PENDING: '待处理',
  QUEUED: '已排队',
  DELIVERED: '已下发',
  DOWNLOADING: '下载中',
  INSTALLING: '安装中',
  REBOOTING: '重启中',
  SUCCESS: '成功',
  FAILED: '失败',
  CANCELLED: '已取消',
};

const Tasks = () => {
  const [tasks, setTasks] = useState([]);
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [currentTask, setCurrentTask] = useState(null);
  const navigate = useNavigate();

  const fetchData = async () => {
    setLoading(true);
    try {
      const [tasksRes, devicesRes] = await Promise.all([
        api.get('/tasks'),
        api.get('/devices'),
      ]);
      setTasks(tasksRes.data);
      setDevices(devicesRes.data);
    } catch (error) {
      message.error('获取数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 5000);
    return () => clearInterval(interval);
  }, []);

  const getDeviceName = (id) => {
    const d = devices.find(d => d.id === id);
    return d ? d.name : id;
  };

  const handleView = (record) => {
    setCurrentTask(record);
    setDetailVisible(true);
  };

  const handleViewLogs = (taskId) => {
    navigate(`/logs/${taskId}`);
  };

  const columns = [
    {
      title: '设备',
      dataIndex: 'deviceId',
      key: 'deviceId',
      render: (id) => getDeviceName(id),
    },
    {
      title: '目标版本',
      dataIndex: 'targetVersion',
      key: 'targetVersion',
    },
    {
      title: '批次',
      dataIndex: 'batch',
      key: 'batch',
    },
    {
      title: '重试次数',
      dataIndex: 'retryCount',
      key: 'retryCount',
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
      title: '完成时间',
      dataIndex: 'completeTime',
      key: 'completeTime',
      render: (val) => val || '-',
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="small">
          <Button type="link" icon={<InfoCircleOutlined />} onClick={() => handleViewLogs(record.id)}>
            日志
          </Button>
          <Button type="link" icon={<EyeOutlined />} onClick={() => handleView(record)}>
            详情
          </Button>
        </Space>
      ),
    },
  ];

  const stats = {
    total: tasks.length,
    success: tasks.filter(t => t.status === 'SUCCESS').length,
    failed: tasks.filter(t => t.status === 'FAILED').length,
    running: tasks.filter(t => ['DELIVERED', 'DOWNLOADING', 'INSTALLING', 'REBOOTING'].includes(t.status)).length,
  };

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', gap: 16 }}>
        <Tag color="blue">总计: {stats.total}</Tag>
        <Tag color="green">成功: {stats.success}</Tag>
        <Tag color="red">失败: {stats.failed}</Tag>
        <Tag color="processing">进行中: {stats.running}</Tag>
      </div>

      <div style={{ marginBottom: 16 }}>
        <Button icon={<ReloadOutlined />} onClick={fetchData}>
          刷新
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={tasks}
        rowKey="id"
        loading={loading}
      />

      <Modal
        title="任务详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
      >
        {currentTask && (
          <Descriptions bordered column={1}>
            <Descriptions.Item label="设备">{getDeviceName(currentTask.deviceId)}</Descriptions.Item>
            <Descriptions.Item label="目标版本">{currentTask.targetVersion}</Descriptions.Item>
            <Descriptions.Item label="批次">{currentTask.batch}</Descriptions.Item>
            <Descriptions.Item label="重试次数">{currentTask.retryCount}</Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={statusColors[currentTask.status]}>
                {statusNames[currentTask.status]}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="创建时间">{currentTask.createTime}</Descriptions.Item>
            <Descriptions.Item label="开始时间">{currentTask.startTime || '-'}</Descriptions.Item>
            <Descriptions.Item label="完成时间">{currentTask.completeTime || '-'}</Descriptions.Item>
            {currentTask.errorMessage && (
              <Descriptions.Item label="错误信息">{currentTask.errorMessage}</Descriptions.Item>
            )}
          </Descriptions>
        )}
      </Modal>
    </div>
  );
};

export default Tasks;
