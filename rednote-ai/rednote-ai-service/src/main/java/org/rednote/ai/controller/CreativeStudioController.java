package org.rednote.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.ai.api.dto.CreativeDraftGenerateRequestDTO;
import org.rednote.ai.api.dto.CreativeOutlineGenerateRequestDTO;
import org.rednote.ai.api.dto.CreativeProjectCreateResponseDTO;
import org.rednote.ai.api.dto.CreativeProjectDocsUpsertRequestDTO;
import org.rednote.ai.api.vo.CreativeDraftVO;
import org.rednote.ai.api.vo.CreativeOutlineVO;
import org.rednote.ai.api.vo.CreativeProjectDocsUpsertVO;
import org.rednote.ai.service.creative.CreativeStudioService;
import org.rednote.common.domain.dto.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "智能创作台", description = "提需求/生成大纲/生成成稿/上传文档")
@RequestMapping("/web/ai/creative")
@RestController
@RequiredArgsConstructor
public class CreativeStudioController {

    private final CreativeStudioService creativeStudioService;

    @Operation(summary = "创建创作项目")
    @PostMapping("project")
    public Result<CreativeProjectCreateResponseDTO> createProject() {
        return Result.ok(creativeStudioService.createProject());
    }

    @Operation(summary = "项目文档入库（PDF 解析 + 向量化）")
    @PostMapping("project/{projectId}/docs")
    public Result<CreativeProjectDocsUpsertVO> upsertDocs(
            @PathVariable("projectId") String projectId,
            @RequestBody CreativeProjectDocsUpsertRequestDTO request) {
        return Result.ok(creativeStudioService.upsertDocs(projectId, request));
    }

    @Operation(summary = "生成大纲（带引用来源）")
    @PostMapping("outline")
    public Result<CreativeOutlineVO> generateOutline(@RequestBody CreativeOutlineGenerateRequestDTO request) {
        return Result.ok(creativeStudioService.generateOutline(request));
    }

    @Operation(summary = "生成成稿（不包含引用）")
    @PostMapping("draft")
    public Result<CreativeDraftVO> generateDraft(@RequestBody CreativeDraftGenerateRequestDTO request) {
        return Result.ok(creativeStudioService.generateDraft(request));
    }

    @Operation(summary = "清理项目临时数据")
    @PostMapping("project/{projectId}/cleanup")
    public Result<Void> cleanup(@PathVariable("projectId") String projectId) {
        creativeStudioService.cleanup(projectId);
        return Result.ok();
    }
}
