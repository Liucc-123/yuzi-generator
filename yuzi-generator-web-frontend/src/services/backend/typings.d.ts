declare namespace API {
  type BaseResponseBoolean_ = {
    code?: number;
    data?: boolean;
    message?: string;
  };

  type BaseResponseGeneratorVO_ = {
    code?: number;
    data?: GeneratorVO;
    message?: string;
  };

  type BaseResponseLoginUserVO_ = {
    code?: number;
    data?: LoginUserVO;
    message?: string;
  };

  type BaseResponseLong_ = {
    code?: number;
    data?: number;
    message?: string;
  };

  type BaseResponsePageGenerator_ = {
    code?: number;
    data?: PageGenerator_;
    message?: string;
  };

  type BaseResponsePageGeneratorVO_ = {
    code?: number;
    data?: PageGeneratorVO_;
    message?: string;
  };

  type BaseResponsePageUser_ = {
    code?: number;
    data?: PageUser_;
    message?: string;
  };

  type BaseResponsePageUserVO_ = {
    code?: number;
    data?: PageUserVO_;
    message?: string;
  };

  type BaseResponseString_ = {
    code?: number;
    data?: string;
    message?: string;
  };

  type BaseResponseUser_ = {
    code?: number;
    data?: User;
    message?: string;
  };

  type BaseResponseUserVO_ = {
    code?: number;
    data?: UserVO;
    message?: string;
  };

  type DeleteRequest = {
    id?: number;
  };

  type downloadUsingGETParams = {
    /** id */
    id?: number;
  };

  type FileConfigDTO = {
    files?: FileInfo[];
    inputRootPath?: string;
    outputRootPath?: string;
    sourceRootPath?: string;
    type?: string;
  };

  type FileInfo = {
    condition?: string;
    files?: FileInfo[];
    generateType?: string;
    groupKey?: string;
    groupName?: string;
    inputPath?: string;
    outputPath?: string;
    type?: string;
  };

  type Generator = {
    author?: string;
    basePackage?: string;
    createTime?: string;
    description?: string;
    distPath?: string;
    fileConfig?: string;
    id?: number;
    isDelete?: number;
    modelConfig?: string;
    name?: string;
    picture?: string;
    status?: number;
    tags?: string;
    updateTime?: string;
    userId?: number;
    version?: string;
  };

  type GeneratorAddRequest = {
    author?: string;
    basePackage?: string;
    createTime?: string;
    description?: string;
    distPath?: string;
    fileConfig?: FileConfigDTO;
    modelConfig?: ModelConfigDTO;
    name?: string;
    picture?: string;
    tags?: string[];
    updateTime?: string;
    userId?: number;
    version?: string;
  };

  type GeneratorCacheRequest = {
    id?: number;
  };

  type GeneratorEditRequest = {
    author?: string;
    basePackage?: string;
    createTime?: string;
    description?: string;
    distPath?: string;
    fileConfig?: FileConfigDTO;
    id?: number;
    modelConfig?: ModelConfigDTO;
    name?: string;
    picture?: string;
    status?: number;
    tags?: string[];
    updateTime?: string;
    userId?: number;
    version?: string;
  };

  type GeneratorMakeRequest = {
    meta?: Meta;
    zipFilePath?: string;
  };

  type GeneratorQueryRequest = {
    author?: string;
    basePackage?: string;
    createTime?: string;
    current?: number;
    description?: string;
    distPath?: string;
    fileConfig?: FileConfigDTO;
    id?: number;
    modelConfig?: ModelConfigDTO;
    name?: string;
    pageSize?: number;
    picture?: string;
    searchText?: string;
    sortField?: string;
    sortOrder?: string;
    status?: number;
    tags?: string[];
    title?: string;
    updateTime?: string;
    userId?: number;
    version?: string;
  };

  type GeneratorUpdateRequest = {
    author?: string;
    basePackage?: string;
    createTime?: string;
    description?: string;
    distPath?: string;
    fileConfig?: FileConfigDTO;
    id?: number;
    modelConfig?: ModelConfigDTO;
    name?: string;
    picture?: string;
    status?: number;
    tags?: string[];
    updateTime?: string;
    userId?: number;
    version?: string;
  };

  type GeneratorUseRequest = {
    dataModel?: Record<string, any>;
    id?: number;
  };

  type GeneratorVO = {
    author?: string;
    basePackage?: string;
    content?: string;
    createTime?: string;
    description?: string;
    distPath?: string;
    fileConfig?: FileConfigDTO;
    id?: number;
    modelConfig?: ModelConfigDTO;
    name?: string;
    picture?: string;
    searchText?: string;
    status?: number;
    tags?: string[];
    title?: string;
    updateTime?: string;
    user?: UserVO;
    userId?: number;
    version?: string;
  };

  type getGeneratorVOByIdUsingGETParams = {
    /** id */
    id?: number;
  };

  type getUserByIdUsingGETParams = {
    /** id */
    id?: number;
  };

  type getUserVOByIdUsingGETParams = {
    /** id */
    id?: number;
  };

  type Git = {
    enable?: boolean;
    gitignore?: string;
  };

  type LoginUserVO = {
    createTime?: string;
    id?: number;
    updateTime?: string;
    userAvatar?: string;
    userName?: string;
    userProfile?: string;
    userRole?: string;
  };

  type Meta = {
    author?: string;
    basePackage?: string;
    createTime?: string;
    description?: string;
    fileConfig?: FileConfigDTO;
    git?: Git;
    modelConfig?: ModelConfigDTO;
    name?: string;
    version?: string;
  };

  type ModelConfigDTO = {
    models?: ModelInfo[];
  };

  type ModelInfo = {
    abbr?: string;
    allArgsStr?: string;
    condition?: string;
    defaultValue?: Record<string, any>;
    description?: string;
    fieldName?: string;
    groupKey?: string;
    groupName?: string;
    models?: ModelInfo[];
    type?: string;
  };

  type OrderItem = {
    asc?: boolean;
    column?: string;
  };

  type PageGenerator_ = {
    countId?: string;
    current?: number;
    maxLimit?: number;
    optimizeCountSql?: boolean;
    orders?: OrderItem[];
    pages?: number;
    records?: Generator[];
    searchCount?: boolean;
    size?: number;
    total?: number;
  };

  type PageGeneratorVO_ = {
    countId?: string;
    current?: number;
    maxLimit?: number;
    optimizeCountSql?: boolean;
    orders?: OrderItem[];
    pages?: number;
    records?: GeneratorVO[];
    searchCount?: boolean;
    size?: number;
    total?: number;
  };

  type PageUser_ = {
    countId?: string;
    current?: number;
    maxLimit?: number;
    optimizeCountSql?: boolean;
    orders?: OrderItem[];
    pages?: number;
    records?: User[];
    searchCount?: boolean;
    size?: number;
    total?: number;
  };

  type PageUserVO_ = {
    countId?: string;
    current?: number;
    maxLimit?: number;
    optimizeCountSql?: boolean;
    orders?: OrderItem[];
    pages?: number;
    records?: UserVO[];
    searchCount?: boolean;
    size?: number;
    total?: number;
  };

  type testFileDownloadUsingGETParams = {
    /** filePath */
    filePath: string;
  };

  type uploadFileUsingPOSTParams = {
    biz?: string;
  };

  type User = {
    createTime?: string;
    id?: number;
    isDelete?: number;
    updateTime?: string;
    userAccount?: string;
    userAvatar?: string;
    userName?: string;
    userPassword?: string;
    userProfile?: string;
    userRole?: string;
  };

  type UserAddRequest = {
    userAccount?: string;
    userAvatar?: string;
    userName?: string;
    userRole?: string;
  };

  type UserLoginRequest = {
    userAccount?: string;
    userPassword?: string;
  };

  type UserQueryRequest = {
    current?: number;
    id?: number;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
    userName?: string;
    userProfile?: string;
    userRole?: string;
  };

  type UserRegisterRequest = {
    checkPassword?: string;
    userAccount?: string;
    userPassword?: string;
  };

  type UserUpdateMyRequest = {
    userAvatar?: string;
    userName?: string;
    userProfile?: string;
  };

  type UserUpdateRequest = {
    id?: number;
    userAvatar?: string;
    userName?: string;
    userProfile?: string;
    userRole?: string;
  };

  type UserVO = {
    createTime?: string;
    id?: number;
    userAvatar?: string;
    userName?: string;
    userProfile?: string;
    userRole?: string;
  };
}
