import CreateModal from '@/pages/Admin/Generator/components/CreateModal';
import UpdateModal from '@/pages/Admin/Generator/components/UpdateModal';
import { deleteGeneratorUsingPost, listGeneratorByPageUsingPost } from '@/services/backend/generatorController';
import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import '@umijs/max';
import { Button, message, Select, Space, Tag, Typography } from 'antd';
import React, { useRef, useState } from 'react';

/**
 * 生成器管理页面
 *
 * @constructor
 */
const GeneratorAdminPage: React.FC = () => {
  // 是否显示新建窗口
  const [createModalVisible, setCreateModalVisible] = useState<boolean>(false);
  // 是否显示更新窗口
  const [updateModalVisible, setUpdateModalVisible] = useState<boolean>(false);
  const actionRef = useRef<ActionType>();
  // 当前用户点击的数据
  const [currentRow, setCurrentRow] = useState<API.Generator>();

  /**
   * 删除节点
   *
   * @param row
   */
  const handleDelete = async (row: API.Generator) => {
    const hide = message.loading('正在删除');
    if (!row) return true;
    try {
      await deleteGeneratorUsingPost({
        id: row.id as any,
      });
      hide();
      message.success('删除成功');
      actionRef?.current?.reload();
      return true;
    } catch (error: any) {
      hide();
      message.error('删除失败，' + error.message);
      return false;
    }
  };

  /**
   * 表格列配置
   * 
  "tags": "[\"Java\"]",
  "distPath": null,
  "status": 0,
  "userId": "1",
  "createTime": "2025-04-28T07:04:19.000+00:00",
  "updateTime": "2025-04-28T07:04:19.000+00:00",
  "isDelete": 0
}
   */
  const columns: ProColumns<API.Generator>[] = [
    {
      title: 'id',
      dataIndex: 'id',
      valueType: 'text',
      hideInForm: true,
    },
    {
      title: '名称',
      dataIndex: 'name',
      valueType: 'text',
    },
    {
      title: '描述',
      dataIndex: 'description',
      valueType: 'text',
    },
    {
      title: '基础包',
      dataIndex: 'basePackage',
      valueType: 'text',
    },
    {
      title: '版本号',
      dataIndex: 'version',
      valueType: 'text',
    },
    {
      title: '作者',
      dataIndex: 'author',
      valueType: 'text',
    },
    {
      title: '标签',
      dataIndex: 'tags',
      valueType: 'text',
      renderFormItem(schema) {
        const { fieldProps } = schema;
        // @ts-ignore
        return <Select {...fieldProps} mode="tags" />;
      },
      render(_, record) {
        if (!record.tags) return <></>;
        return JSON.parse(record.tags).map((tag: string) => {
          return <Tag key={tag}>{tag}</Tag>;
        })
      }
    },
    {
      title: '文件配置',
      dataIndex: 'fileConfig',
      valueType: 'jsonCode',
      hideInSearch: true,
    },
    {
      title: '模型配置',
      dataIndex: 'modelConfig',
      valueType: 'jsonCode',
      hideInSearch: true,
    },
    {
      title: '产物路径',
      dataIndex: 'distPath',
      valueType: 'text',
      hideInSearch: true,
    },
    {
      title: '图片',
      dataIndex: 'picture',
      valueType: 'image',
      fieldProps: {
        width: 64,
      },
      hideInSearch: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      valueEnum: {
        0: {
          text: '默认',
        }
      },
    },
    {
      title: '创建用户',
      dataIndex: 'userId',
      valueType: 'text',
      hideInForm: true,
    },
    {
      title: '创建时间',
      sorter: true,
      dataIndex: 'createTime',
      valueType: 'dateTime',
      hideInSearch: true,
      hideInForm: true,
    },
    {
      title: '更新时间',
      sorter: true,
      dataIndex: 'updateTime',
      valueType: 'dateTime',
      hideInSearch: true,
      hideInForm: true,
    },
    {
      title: '操作',
      dataIndex: 'option',
      valueType: 'option',
      render: (_, record) => (
        <Space size="middle">
          <Typography.Link
            onClick={() => {
              setCurrentRow(record);
              setUpdateModalVisible(true);
            }}
          >
            修改
          </Typography.Link>
          <Typography.Link type="danger" onClick={() => handleDelete(record)}>
            删除
          </Typography.Link>
        </Space>
      ),
    },
  ];
  return (
    <div className='generator-admin-page'>
      <Typography.Title level={4} style={{ marginBottom: 16 }}>生成器管理</Typography.Title>
      <ProTable<API.Generator>
        headerTitle={'查询表格'}
        actionRef={actionRef}
        rowKey="key"
        search={{
          labelWidth: 120,
        }}
        toolBarRender={() => [
          <Button
            type="primary"
            key="primary"
            onClick={() => {
              setCreateModalVisible(true);
            }}
          >
            <PlusOutlined /> 新建
          </Button>,
        ]}
        request={async (params, sort, filter) => {
          const sortField = Object.keys(sort)?.[0];
          const sortOrder = sort?.[sortField] ?? undefined;

          const { data, code } = await listGeneratorByPageUsingPost({
            ...params,
            sortField,
            sortOrder,
            ...filter,
          } as API.GeneratorQueryRequest);

          return {
            success: code === 0,
            data: data?.records || [],
            total: Number(data?.total) || 0,
          };
        }}
        columns={columns}
      />
      <CreateModal
        visible={createModalVisible}
        columns={columns}
        onSubmit={() => {
          setCreateModalVisible(false);
          actionRef.current?.reload();
        }}
        onCancel={() => {
          setCreateModalVisible(false);
        }}
      />
      <UpdateModal
        visible={updateModalVisible}
        columns={columns}
        oldData={currentRow}
        onSubmit={() => {
          setUpdateModalVisible(false);
          setCurrentRow(undefined);
          actionRef.current?.reload();
        }}
        onCancel={() => {
          setUpdateModalVisible(false);
        }}
      />
    </div>
  );
};
export default GeneratorAdminPage;
