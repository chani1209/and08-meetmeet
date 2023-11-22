import {
  Body,
  Controller,
  Delete,
  Get,
  Patch,
  UploadedFile,
  UseGuards,
  UseInterceptors,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import {
  ApiBearerAuth,
  ApiConsumes,
  ApiOperation,
  ApiTags,
} from '@nestjs/swagger';
import { GetUser } from 'src/auth/get-user.decorator';
import { JwtAuthGuard } from 'src/auth/jwt-auth.guard';
import { multerOptions } from 'src/common/config/multer.config';
import { UpdateUserDto } from './dto/update-user.dto';
import { User } from './entities/user.entity';
import { UserService } from './user.service';

@ApiBearerAuth()
@ApiTags('user')
@Controller('user')
export class UserController {
  constructor(private readonly userService: UserService) {}

  @UseGuards(JwtAuthGuard)
  @Get('info')
  @ApiOperation({
    summary: '사용자 프로필 조회 API',
    description: 'kakao 회원의 경우 email에 kakao라고 조회됩니다.',
  })
  getUserInfo(@GetUser() user: User) {
    return this.userService.getUserInfo(user);
  }

  @UseGuards(JwtAuthGuard)
  @UseInterceptors(FileInterceptor('profile', multerOptions))
  @Patch('info')
  @ApiOperation({
    summary: '사용자 프로필, 계정 수정 API',
    description: 'parameter의 id와 access token의 user id가 같아야 합니다.',
  })
  @ApiConsumes('multipart/form-data')
  updateUserInfo(
    @UploadedFile() profileImage: Express.Multer.File,
    @GetUser() user: User,
    @Body() updateUserDto: UpdateUserDto,
  ) {
    return this.userService.updateUser(user, updateUserDto, profileImage);
  }

  @UseGuards(JwtAuthGuard)
  @Delete()
  @ApiOperation({
    summary: '사용자 탈퇴 API',
    description: 'parameter의 id와 access token의 user id가 같아야 합니다.',
  })
  deleteUser(@GetUser() user: User) {
    return this.userService.deleteUser(user);
  }
}
