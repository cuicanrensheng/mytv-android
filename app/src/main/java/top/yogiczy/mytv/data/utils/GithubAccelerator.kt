package top.yogiczy.mytv.data.utils

import java.util.regex.Pattern

/**
 * ✅ GitHub 直播源智能加速器
 *
 * 【功能说明】
 * 自动识别 GitHub 链接，自动转换为 CDN 加速地址，大幅提升下载速度。
 *
 * 【支持识别的 GitHub 链接格式】
 * 1. raw.githubusercontent.com/user/repo/branch/file
 * 2. github.com/user/repo/raw/branch/file
 * 3. raw.github.com/user/repo/branch/file
 *
 * 【支持的加速源】
 * - jsDelivr CDN（推荐，全球加速，国内也快）
 * - ghproxy（GitHub 反向代理）
 * - gitmirror（GitHub 镜像站）
 * - 不加速（直连）
 */
object GithubAccelerator {

    /**
     * 当前使用的加速源类型
     */
    private var accelerateType: Constants.GithubAccelerateType =
        Constants.DEFAULT_GITHUB_ACCELERATE_TYPE

    /**
     * 是否启用 GitHub 加速
     */
    private var accelerateEnabled: Boolean = Constants.DEFAULT_GITHUB_ACCELERATE_ENABLED

    // ====================================================================
    // 配置相关方法
    // ====================================================================

    /**
     * 设置是否启用 GitHub 加速
     *
     * @param enabled 是否启用
     */
    fun setAccelerateEnabled(enabled: Boolean) {
        accelerateEnabled = enabled
    }

    /**
     * 设置加速源类型
     *
     * @param type 加速源类型
     */
    fun setAccelerateType(type: Constants.GithubAccelerateType) {
        accelerateType = type
    }

    /**
     * 获取当前加速源名称
     */
    fun getAccelerateTypeName(): String {
        return when (accelerateType) {
            Constants.GithubAccelerateType.JSDELIVR -> "jsDelivr CDN"
            Constants.GithubAccelerateType.GHPROXY -> "ghproxy"
            Constants.GithubAccelerateType.GITMIRROR -> "gitmirror"
            Constants.GithubAccelerateType.NONE -> "不加速（直连）"
        }
    }

    // ====================================================================
    // 核心方法：获取加速后的 URL
    // ====================================================================

    /**
     * 获取加速后的 URL
     *
     * 【智能识别逻辑】
     * 1. 如果加速功能禁用，直接返回原地址
     * 2. 如果不是 GitHub 链接，直接返回原地址
     * 3. 如果是 GitHub 链接，根据配置的加速源类型转换
     *
     * @param originalUrl 原始 URL
     * @return 处理后的 URL
     */
    fun getAcceleratedUrl(originalUrl: String?): String {
        // 空地址直接返回
        if (originalUrl.isNullOrBlank()) {
            return originalUrl ?: ""
        }

        // 加速功能禁用，直接返回
        if (!accelerateEnabled) {
            return originalUrl
        }

        // 检查是否是 GitHub 链接
        if (!isGitHubUrl(originalUrl)) {
            return originalUrl
        }

        // 根据加速源类型转换
        return when (accelerateType) {
            Constants.GithubAccelerateType.JSDELIVR -> convertToJsdelivr(originalUrl)
            Constants.GithubAccelerateType.GHPROXY -> convertToGhproxy(originalUrl)
            Constants.GithubAccelerateType.GITMIRROR -> convertToGitmirror(originalUrl)
            Constants.GithubAccelerateType.NONE -> originalUrl
        }
    }

    // ====================================================================
    // GitHub 链接识别
    // ====================================================================

    /**
     * 判断是否是 GitHub 链接
     *
     * 支持识别的格式：
     * 1. raw.githubusercontent.com/user/repo/branch/file
     * 2. github.com/user/repo/raw/branch/file
     * 3. raw.github.com/user/repo/branch/file
     *
     * @param url 要检查的 URL
     * @return 是否是 GitHub 链接
     */
    fun isGitHubUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false

        return url.contains("raw.githubusercontent.com")
                || (url.contains("github.com/") && url.contains("/raw/"))
                || url.contains("raw.github.com")
    }

    // ====================================================================
    // 加速源 1：jsDelivr CDN（推荐）
    // ====================================================================

    /**
     * 转换成 jsDelivr CDN 地址
     *
     * 【格式说明】
     * GitHub raw: https://raw.githubusercontent.com/user/repo/branch/file
     * jsDelivr:   https://cdn.jsdelivr.net/gh/user/repo@branch/file
     *
     * 【优点】
     * - 全球 CDN，速度快
     * - 国内也有节点，访问速度不错
     * - 支持缓存，加载更快
     *
     * @param githubUrl GitHub 原始地址
     * @return jsDelivr 加速地址
     */
    private fun convertToJsdelivr(githubUrl: String): String {
        return try {
            val info = parseGitHubUrl(githubUrl)
                ?: return githubUrl  // 解析失败，返回原地址

            // 组装 jsDelivr 地址
            // 格式：https://cdn.jsdelivr.net/gh/user/repo@branch/path
            buildString {
                append("https://cdn.jsdelivr.net/gh/")
                append(info.user)
                append("/")
                append(info.repo)
                if (!info.branch.isNullOrBlank()) {
                    append("@")
                    append(info.branch)
                }
                append("/")
                append(info.path)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            githubUrl  // 转换失败，返回原地址
        }
    }

    // ====================================================================
    // 加速源 2：ghproxy（GitHub 反向代理）
    // ====================================================================

    /**
     * 转换成 ghproxy 地址
     *
     * 【格式说明】
     * 直接在原 URL 前面加上 https://ghproxy.com/
     *
     * 【优点】
     * - 支持所有 GitHub 链接
     * - 不需要转换格式
     *
     * @param githubUrl GitHub 原始地址
     * @return ghproxy 加速地址
     */
    private fun convertToGhproxy(githubUrl: String): String {
        return try {
            "https://ghproxy.com/$githubUrl"
        } catch (e: Exception) {
            e.printStackTrace()
            githubUrl
        }
    }

    // ====================================================================
    // 加速源 3：gitmirror（GitHub 镜像站）
    // ====================================================================

    /**
     * 转换成 gitmirror 镜像地址
     *
     * 【格式说明】
     * raw.githubusercontent.com → raw.gitmirror.com
     *
     * 【优点】
     * - 国内镜像站，速度快
     * - 格式简单，只换域名
     *
     * @param githubUrl GitHub 原始地址
     * @return gitmirror 加速地址
     */
    private fun convertToGitmirror(githubUrl: String): String {
        return try {
            githubUrl.replace("raw.githubusercontent.com", "raw.gitmirror.com")
                .replace("raw.github.com", "raw.gitmirror.com")
        } catch (e: Exception) {
            e.printStackTrace()
            githubUrl
        }
    }

    // ====================================================================
    // GitHub URL 解析工具
    // ====================================================================

    /**
     * GitHub URL 信息
     */
    private data class GitHubUrlInfo(
        val user: String,      // 用户名
        val repo: String,      // 仓库名
        val branch: String?,   // 分支名
        val path: String       // 文件路径
    )

    /**
     * 解析 GitHub URL
     *
     * 支持的格式：
     * 1. https://raw.githubusercontent.com/user/repo/branch/path/to/file
     * 2. https://github.com/user/repo/raw/branch/path/to/file
     * 3. https://raw.github.com/user/repo/branch/path/to/file
     *
     * @param url GitHub URL
     * @return 解析后的信息，解析失败返回 null
     */
    private fun parseGitHubUrl(url: String): GitHubUrlInfo? {
        if (url.isBlank()) return null

        return try {
            // 去掉 https:// 或 http:// 前缀
            val cleanUrl = when {
                url.startsWith("https://") -> url.substring(8)
                url.startsWith("http://") -> url.substring(7)
                else -> url
            }

            // 格式 1：raw.githubusercontent.com/user/repo/branch/path
            if (cleanUrl.startsWith("raw.githubusercontent.com/")) {
                val pathPart = cleanUrl.removePrefix("raw.githubusercontent.com/")
                val parts = pathPart.split("/", limit = 4)
                if (parts.size >= 4) {
                    return GitHubUrlInfo(
                        user = parts[0],
                        repo = parts[1],
                        branch = parts[2],
                        path = parts[3]
                    )
                }
            }

            // 格式 2：github.com/user/repo/raw/branch/path
            if (cleanUrl.startsWith("github.com/") && cleanUrl.contains("/raw/")) {
                val pattern = Pattern.compile("github\\.com/([^/]+)/([^/]+)/raw/([^/]+)/(.+)")
                val matcher = pattern.matcher(cleanUrl)
                if (matcher.find()) {
                    return GitHubUrlInfo(
                        user = matcher.group(1)!!,
                        repo = matcher.group(2)!!,
                        branch = matcher.group(3),
                        path = matcher.group(4)!!
                    )
                }
            }

            // 格式 3：raw.github.com/user/repo/branch/path
            if (cleanUrl.startsWith("raw.github.com/")) {
                val pathPart = cleanUrl.removePrefix("raw.github.com/")
                val parts = pathPart.split("/", limit = 4)
                if (parts.size >= 4) {
                    return GitHubUrlInfo(
                        user = parts[0],
                        repo = parts[1],
                        branch = parts[2],
                        path = parts[3]
                    )
                }
            }

            // 都没匹配上
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
