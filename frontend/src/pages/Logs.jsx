import React, { useState, useEffect } from 'react';
import { Table, Tag, Space, Button, message, Card, Typography, Descriptions } from 'antd';
import { ArrowLeftOutlined, ReloadOutlined } from '@ant-design/icons';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api';

const { Title } = Typography;

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

const Logs = () => {
  const { taskId } = useParams();
  const navigate = useNavigate();
  const [logs, setLogs] = useState([]);
  const [task, setTask] = useState(null);
  const [loading, setLoading] = useState(false);

  const fetchLogs = async () => {
    setLoading(true);
    try {
      const [logsRes, taskRes] = await Promise.all([
        api.get(`/logs/task/${taskId}`),
        api.get(`/tasks/${taskId}`),
      ]);
      setLogs(logsRes.data);
      setTask(taskRes.data);
    } catch (error) {
      message.error('获取日志失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
    const interval = setInterval(fetchLogs, 3000);
    return () => clearInterval(interval);
  }, [taskId]);

  const columns = [
    {
      title: '时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 200,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status) => (
        <Tag color={statusColors[status]}>
          {statusNames[status]}
        </Tag>
      ),
    },
    {
      title: '消息',
      dataIndex: 'message',
      key: 'message',
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/tasks')}>
            返回任务列表
          </Button>
          <Button icon={<ReloadOutlined />} onClick={fetchLogs}>
            刷新
          </Button>
        </Space>
      </div>

      {task && (
        <Card style={{ marginBottom: 24 }}>
          <Title level={5}>任务信息</Title>
          <Descriptions column={4} size="small">
            <Descriptions.Item label="任务ID">{task.id}</Descriptions.Item>
            <Descriptions.Item label="目标版本">{task.targetVersion}</Descriptions.Item>
            <Descriptions.Item label="批次">{task.batch}</Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={statusColors[task.status]}>
                {statusNames[task.status]}
              </Tag>
            </Descriptions.Item>
          </Descriptions>
        </Card>
      )}

      <Card title="升级日志">
        <Table
          columns={columns}
          dataSource={logs}
          rowKey="id"
          loading={loading}
          pagination={false}
        />
      </Card>
    </div>
  );
};

export default Logs;
