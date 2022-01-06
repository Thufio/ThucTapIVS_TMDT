package com.tmdt.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.opensymphony.xwork2.ActionSupport;
import com.tmdt.db.ConnectDB;
import com.tmdt.errors.CustomError;
import com.tmdt.utilities.JsonResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.*;

import mybatis.mapper.AnhSanPhamMapper;
import mybatis.mapper.SanPhamMapper;

@Result(name = "input", location = "/index", type = "redirectAction", params = {
        "namespace", "/",
        "actionName", "bad-request"
})
public class SanPhamAction extends ActionSupport {
    private static final long serialVersionUID = 1L;
    private String maSanPham;
    private int status;
    private String search;
    private int page;
    private int rowsPerPage;

    // region Getter and Setter
    public String getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(String maSanPham) {
        this.maSanPham = maSanPham;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public int getPage() {
        if (page < 1) {
            page = 1;
        }
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getRowsPerPage() {
        switch (rowsPerPage) {
            case 5:
            case 10:
            case 20:
            case 30:
                return rowsPerPage;
            default:
                return 5;
        }
    }

    public void setRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }
    // endregion

    HttpServletResponse response = ServletActionContext.getResponse();
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpSession session = request.getSession();

    private SqlSessionFactory sqlSessionFactory = ConnectDB.getSqlSessionFactory();

    // Danh sách sản phẩm
    @Action(value = "/api/v1/sanpham/getall", results = {
            @Result(name = "success", location = "/index.html")
    })
    public String getAllSanPhams() throws IOException {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        SanPhamMapper sanPhamMapper = sqlSession.getMapper(SanPhamMapper.class);
        List<Map<String, Object>> listSanPham = sanPhamMapper.getAllSanPham();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("sanphams", listSanPham);
        return JsonResponse.createJsonResponse(map, 200, response);
    }

    // Chi tiết sản phẩm
    @Action(value = "/api/v1/sanpham/{maSanPham}", results = {
            @Result(name = SUCCESS, location = "/index.html")
    })
    public String chiTietSanPhams() throws IOException {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        SanPhamMapper sanPhamMapper = sqlSession.getMapper(SanPhamMapper.class);
        AnhSanPhamMapper anhSanPhamMapper = sqlSession.getMapper(AnhSanPhamMapper.class);

        Map<String, Object> sanPham = sanPhamMapper.getDetailSanPham(maSanPham);
        Map<String, Object> jsonRes = new HashMap<String, Object>();

        if (sanPham == null) {
            jsonRes.put("message", "Không tìm thấy sản phẩm");
            return JsonResponse.createJsonResponse(jsonRes, 404, response);
        }

        List<String> listAnhSanPham = anhSanPhamMapper.getAllAnhSanPham(maSanPham);
        sanPham.put("anhSanPhams", listAnhSanPham);

        jsonRes.put("sanpham", sanPham);
        System.out.println(sanPham);
        return JsonResponse.createJsonResponse(jsonRes, 200, response);
    }

    /**
     * Dành cho nhân viên stack
     * 
     * 
     */

    @Action(value = "/api/v1/sanpham/getbystatus/{status}", results = {
            @Result(name = "success", location = "/index.html")
    }, interceptorRefs = {
            @InterceptorRef(value = "nhanVienStack"),
    })
    public String getAllSanPhamsChuaDuyet() throws IOException {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        SanPhamMapper sanPhamMapper = sqlSession.getMapper(SanPhamMapper.class);
        List<Map<String, Object>> listSanPham = sanPhamMapper.getSP_ByStatus(status);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("sanphams", listSanPham);
        return JsonResponse.createJsonResponse(map, 200, response);
    }

    @Action(value = "/api/v1/sanpham/changestatus", results = {
            @Result(name = "success", location = "/index.html")
    }, interceptorRefs = {
            @InterceptorRef(value = "nhanVienStack"),
    })
    public String changeStatusSP() throws IOException {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        SanPhamMapper sanPhamMapper = sqlSession.getMapper(SanPhamMapper.class);

        if (status >= -1 && status <= 2) {
            int changeStatusSP = sanPhamMapper.updateSP_Status(status, maSanPham);
            if (changeStatusSP == 1) {
                sqlSession.commit();
                sqlSession.close();
                return CustomError.createCustomError("Cập nhật trạng thái sản phẩm thành công", 201, response);
            }
            sqlSession.close();
            return CustomError.createCustomError("Mã sản phẩm không hợp lệ", 401, response);
        }
        // Map<String, Object> map = new HashMap<String, Object>();
        // map.put("sanphams", listSanPham);
        sqlSession.close();
        return CustomError.createCustomError("Trạng thái sản phẩm không hợp lệ", 401, response);
    }

}
